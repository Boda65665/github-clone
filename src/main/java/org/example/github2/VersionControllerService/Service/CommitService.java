package org.example.github2.VersionControllerService.Service;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import org.example.github2.VersionControllerService.Entity.RepositoryTree;
import org.example.github2.VersionControllerService.Models.*;
import org.springframework.stereotype.Service;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class CommitService {
    private final ServiceRepositoryTree serviceRepositoryTree;

    public CommitService(ServiceRepositoryTree serviceRepositoryTree) {
        this.serviceRepositoryTree = serviceRepositoryTree;
    }

    public void addNewCommit(String directoryPath, String nameFile, String newContent, int idRepository) throws IOException {
        String pathToFile =  directoryPath +"/"+nameFile;
        Commit commit = getCommit(pathToFile, newContent);
        if (commit==null) return;
        serviceRepositoryTree.addNewCommit(commit, idRepository);
        setNewContentFile(pathToFile, newContent);
    }

    public static String generateHashId() {
        try {
            String randomString = UUID.randomUUID().toString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(randomString.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        }
        catch (NoSuchAlgorithmException err){
            throw new RuntimeException(err.getMessage());
        }
    }

    private void setNewContentFile(String filePath, String newContent) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(newContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Commit getCommit(List<Delta> deltas) {
        if (deltas.isEmpty()) return null;
        Commit commit = new Commit();
        for (Delta delta : deltas) {
            commit.addChange(getChange(delta));
        }
        return commit;
    }

    private Change getChange(Delta delta) {
        return new Change(delta.getNumberLine(),delta.getPathFile(), delta.getAction(), delta.getContent());
    }

    public Commit getCommit(String originalPathFile, String newContent) throws IOException {
        List<Delta> deltas = getDelta(originalPathFile, newContent);
        return getCommit(deltas);
    }

    private List<Delta> getDelta(String originalPathFile, String newContent) throws IOException {
        List<String> originalContent = Files.readAllLines(Paths.get(originalPathFile));
        List<String> updateContent = List.of(newContent.split("\n"));
        Patch<String> patch = DiffUtils.diff(originalContent, updateContent);
        List<Delta> deltas = new ArrayList<>();
        for (AbstractDelta<String> delta : patch.getDeltas()) {
            int positionStart = delta.getTarget().getPosition();
            int positionEnd = positionStart + delta.getTarget().size();
            Action action = getAction(delta);
            String editLines = "";
            if (action==Action.ADD_CONTENT_IN_FILE){
                editLines = String.join("\n", updateContent.subList(positionStart,positionEnd));
            }
            else if(action==Action.EDIT_CONTENT_IN_FILE){
                editLines = delta.getSource().getLines().size() + "~" +
                        String.join("\n", updateContent.subList(positionStart,positionEnd)) + "~"
                +deltas.add(new Delta(positionStart ,editLines,originalPathFile,action));
            }
            else {
                editLines =  String.join("\n", delta.getSource().getLines());
            }
            deltas.add(new Delta(positionStart ,editLines,originalPathFile,action));
        }
        return deltas;
    }

    private Action getAction(AbstractDelta<String> delta) {
        return (delta.getType() == DeltaType.DELETE)?Action.DELETE_CONTENT_IN_FILE:
                (delta.getType()==DeltaType.CHANGE)?Action.EDIT_CONTENT_IN_FILE:Action.ADD_CONTENT_IN_FILE;
    }

    public Commit getCommitByHashId(int idRep,String hashId){
        RepositoryTree repositoryTree = serviceRepositoryTree.findById(idRep);
        Commit commit = repositoryTree.getCommit();;
        while (commit!= null && !commit.getHashId().equals(hashId)){
            commit=commit.getNextCommit();
        }
        return commit;

    }
}
