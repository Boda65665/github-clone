package org.example.github2.VersionControllerService.Service;

import org.example.github2.Entity.Repository;
import org.example.github2.Services.DB.RepositoryService;
import org.example.github2.VersionControllerService.Entity.RepositoryTree;
import org.example.github2.VersionControllerService.Models.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoleBackCommitService {
    private final ServiceRepositoryTree serviceRepositoryTree;
    private final RepositoryService repositoryService;
    private final CommitService commitService;
    @Value("${name.disk.with.repository}")
    private String NAME_DISK;

    public RoleBackCommitService(ServiceRepositoryTree serviceRepositoryTree, RepositoryService repositoryService, CommitService commitService) {
        this.serviceRepositoryTree = serviceRepositoryTree;
        this.repositoryService = repositoryService;
        this.commitService = commitService;
    }

    public void roleBackLastCommit(int idRepository){
        RepositoryTree repositoryTree = serviceRepositoryTree.findById(idRepository);
        roleBackCommit(repositoryTree.getCommit(),repositoryTree);
        Commit commit = new Commit();
        commit.addChange(new Change(Action.CANCELED_COMMIT,repositoryTree.getCommit().getHashId()));
        repositoryTree.getCommit().setNextCommit(commit);
        serviceRepositoryTree.update(repositoryTree);
    }

    private void roleBackCommit(Commit commit,RepositoryTree repositoryTree) {
        if (commit.isCanceled()) return;
        commit.setCanceled(true);
        for (Change change : commit.getChanges()) {
            switch (change.getAction()){
                case ADD_DIRECTORY: editDeleteStatusDirectory(change,repositoryTree,true);break;
                case DELETE_DIRECTORY:editDeleteStatusDirectory(change,repositoryTree,false);break;
                case DELETE_CONTENT_IN_FILE:editContentInFile(change,Action.DELETE_CONTENT_IN_FILE,repositoryTree);break;
                case EDIT_CONTENT_IN_FILE:editContentInFile(change,Action.EDIT_CONTENT_IN_FILE,repositoryTree);break;
                case ADD_CONTENT_IN_FILE:editContentInFile(change,Action.ADD_CONTENT_IN_FILE,repositoryTree);break;
                case DELETE_FILE: editDeleteStatusFile(change,repositoryTree,false);break;
                case ADD_FILE:editDeleteStatusFile(change,repositoryTree,true);
            }
        }
    }

    private void editDeleteStatusDirectory(Change change, RepositoryTree repositoryTree, boolean status) {
        String pathToDirectory = change.getLocation();
        Directory directory = getDirectoryByPath(repositoryTree,pathToDirectory);
        directory.setDelete(status);
        serviceRepositoryTree.update(repositoryTree);
    }

    private Directory getDirectoryByPath(RepositoryTree repositoryTree, String pathToDirectory) {
        return serviceRepositoryTree.getDirectoryByPath(repositoryTree,pathToDirectory);
    }

    private void editContentInFile(Change change, Action action,RepositoryTree repositoryTree) {
        Repository repository = repositoryService.findById(repositoryTree.getRepositoryId());
        String pathToFile = NAME_DISK+"/repository/"+repository.getOwner().getLogin()+"/"+repository.getName()+change.getLocation();
        switch (action){
            case DELETE_CONTENT_IN_FILE -> addContentInFile(change,pathToFile);
            case ADD_CONTENT_IN_FILE -> deleteContentInFile(change,pathToFile);
            case EDIT_CONTENT_IN_FILE -> {
                String[] payload = change.getContent().split("~");
                String oldContent = payload[1];
                int countNumber = Integer.parseInt(payload[0]);
                int startIndexLine = change.getNumberLine();
                removeLines(pathToFile,startIndexLine,startIndexLine+countNumber);
                insertLines(pathToFile,oldContent,startIndexLine);
            }
        }
    }

    private void removeLines(String filePath, Integer startLine, Integer endLine) {
        Path path = Paths.get(filePath);
        try {
            List<String> lines = Files.readAllLines(path);
            for (int i = endLine - 1; i >= startLine - 1; i--) { // Индексируем с 0
                if (i < lines.size()) {
                    lines.remove(i);
                }
            }
            Files.write(path, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insertLines(String pathToFile, String oldContent, Integer startIndexLine) {
        Path path = Paths.get(pathToFile);
        try {
            List<String> lines = Files.readAllLines(path);
            lines.add(startIndexLine, oldContent);
            Files.write(path, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addContentInFile(Change change, String pathToFile) {
        try {
            Path path = Paths.get(pathToFile);
            List<String> lines = Files.readAllLines(path);
            if (lines.size() >= change.getNumberLine()) {
                lines.add(change.getNumberLine(), change.getContent());
            } else {
                lines.add(change.getContent());
            }
            Files.write(path, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteContentInFile(Change change,String pathString) {
        try {
            Path path = Paths.get(pathString);
            List<String> originalLines = Files.readAllLines(path);
            List<String> newLines = new ArrayList<>();
            String[] deleteLines = change.getContent().split("\n");
            for (int i = 0; i < originalLines.size(); i++) {
                if(i<change.getNumberLine()||i>=change.getNumberLine()+deleteLines.length){
                    newLines.add(originalLines.get(i));
                }
            }
            Files.write(path, newLines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void editDeleteStatusFile(Change change, RepositoryTree repositoryTree,boolean status){
        String pathFile = change.getLocation();
        File file = getFileFromPath(repositoryTree,pathFile);
        file.setDelete(status);
        serviceRepositoryTree.update(repositoryTree);
    }


    private File getFileFromPath(RepositoryTree repositoryTree, String path) {
        return serviceRepositoryTree.getFileByPath(path,repositoryTree);
    }


    public void roleBackSpecificCommit(int idRepository,String hashId){
        RepositoryTree repositoryTree = serviceRepositoryTree.findById(idRepository);
        Commit commit = commitService.getCommitByHashId(idRepository,hashId);
        roleBackCommit(commit,repositoryTree);
    }
}
