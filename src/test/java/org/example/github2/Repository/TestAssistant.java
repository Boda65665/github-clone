package org.example.github2.Repository;

import org.example.github2.VersionControllerService.Entity.RepositoryTree;
import org.example.github2.VersionControllerService.Models.*;
import org.example.github2.VersionControllerService.Repositories.GitRepRepository;
import org.example.github2.VersionControllerService.Service.ServiceRepositoryTree;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TestAssistant {
    private final GitRepRepository gitRepRepository;
    private final ServiceRepositoryTree serviceRepositoryTree;

    public TestAssistant(GitRepRepository gitRepRepository, ServiceRepositoryTree serviceRepositoryTree) {
        this.gitRepRepository = gitRepRepository;
        this.serviceRepositoryTree = serviceRepositoryTree;
    }

    public void newRepositories(int count) {
        for (int i = 0; i < count; i++) {
            Change change = new Change(new Coordinate(1, 1, 1), Action.ADD);
            Commit commit = new Commit(null, change);
            File file = new File("/", commit);
            File file2 = new File("/2", commit);
            List<File> files = new ArrayList<>();
            files.add(file);
            files.add(file2);
            Directory directory = new Directory("1", files, null);
            Directory directory2 = new Directory("2", files, null);
            List<Directory> directories = new ArrayList<>();
            directories.add(directory);
            directories.add(directory2);
            RepositoryTree repository = new RepositoryTree(files, directories, i);
            gitRepRepository.save(repository);
        }
    }

    public void update(RepositoryTree repositoryTree){
        serviceRepositoryTree.update(repositoryTree);
    }

    public void clearBd(){
        gitRepRepository.deleteAll();
    }
}
