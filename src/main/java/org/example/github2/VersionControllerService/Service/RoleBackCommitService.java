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
import java.util.Arrays;
import java.util.List;

@Service
public class RoleBackCommitService {
    private final ServiceRepositoryTree serviceRepositoryTree;
    private final RepositoryService repositoryService;
    @Value("${name.disk.with.repository}")
    private String NAME_DISK;

    public RoleBackCommitService(ServiceRepositoryTree serviceRepositoryTree, RepositoryService repositoryService) {
        this.serviceRepositoryTree = serviceRepositoryTree;
        this.repositoryService = repositoryService;
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
                case DELETE_DIRECTORY:editDeleteStatusDirectory(change,repositoryTree,false);break;
                case DELETE_CONTENT_IN_FILE:editContentInFile(change,Action.DELETE_CONTENT_IN_FILE,repositoryTree);break;
                case ADD_CONTENT_IN_FILE:{
                    editContentInFile(change,Action.ADD_CONTENT_IN_FILE,repositoryTree);
                    break;
                }
                case DELETE_FILE: editDeleteStatusFile(change,repositoryTree,false);break;
                case ADD_FILE:editDeleteStatusFile(change,repositoryTree,true);break;

//                case DELETE_DIRECTORY:restoreDirectory(change);
//                case EDIT_CONTENT_IN_FILE:restoreContentInFile(change);
            }
        }
    }

    private void editDeleteStatusDirectory(Change change, RepositoryTree repositoryTree, boolean status) {
        String pathToDirectory = change.getLocation();
        Directory directory = getDirectoryByPath(repositoryTree,pathToDirectory);
        directory.setDelete(true);
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
        Commit commit = repositoryTree.getCommit();
        while (commit!=null){
            if (commit.getHashId().equals(hashId)) {
                roleBackCommit(commit,repositoryTree);
                return;
            }
            commit = commit.getNextCommit();
        }
    }
}
