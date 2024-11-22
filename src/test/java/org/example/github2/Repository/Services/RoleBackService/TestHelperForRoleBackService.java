package org.example.github2.Repository.Services.RoleBackService;

import org.example.github2.VersionControllerService.Entity.RepositoryTree;
import org.example.github2.VersionControllerService.Models.*;
import org.example.github2.VersionControllerService.Repositories.GitRepRepository;
import org.example.github2.VersionControllerService.Service.ServiceRepositoryTree;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TestHelperForRoleBackService {
    private final ServiceRepositoryTree serviceRepositoryTree;
    private final GitRepRepository gitRepRepository;

    public TestHelperForRoleBackService(ServiceRepositoryTree serviceRepositoryTree, GitRepRepository gitRepRepository) {
        this.serviceRepositoryTree = serviceRepositoryTree;
        this.gitRepRepository = gitRepRepository;
        clearBd();
    }

    public void createRepositoryWithCommitAction(Action action){
        clearBd();
        RepositoryTree repositoryTree = new RepositoryTree(0);
        switch (action){
            case ADD_DIRECTORY -> {
                createNewDirectory(false,repositoryTree);
                addNewCommit(repositoryTree,Action.ADD_DIRECTORY);
            }
            case DELETE_DIRECTORY -> {
                createNewDirectory(true,repositoryTree);
                addNewCommit(repositoryTree,Action.DELETE_DIRECTORY);
            }
            case ADD_FILE -> {
                createNewFile(false,repositoryTree);
                addNewCommit(repositoryTree, Action.ADD_FILE);
            }
            case DELETE_FILE -> {
                createNewFile(true,repositoryTree);
                addNewCommit(repositoryTree,Action.DELETE_FILE);
            }
        }
        serviceRepositoryTree.save(repositoryTree);
    }

    private void createNewDirectory(boolean isDelete, RepositoryTree repositoryTree) {
        Directory directory = new Directory("testName");
        directory.setDelete(isDelete);
        repositoryTree.addDirectory(directory);
    }

    private void createNewFile(boolean isDelete,RepositoryTree repositoryTree) {
        File file = new File("testName",isDelete);
        repositoryTree.getFiles().add(file);
    }

    private void addNewCommit(RepositoryTree repositoryTree, Action action) {
        addNewCommit(repositoryTree,action,"/testName",null);
    }

    private void addNewCommit(RepositoryTree repositoryTree, Action action,String location,String content) {
        List<Change> changeList = new ArrayList<>();
        Change change = new Change(action,location);
        if (content!=null) change.setContent(content);
        changeList.add(change);
        Commit commit = new Commit(null,changeList,"testHashId","testName");
        repositoryTree.setCommit(commit);
    }

    public void clearBd(){
        gitRepRepository.deleteAll();
    }
}
