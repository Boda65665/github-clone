package org.example.github2.Repository;

import org.example.github2.Entity.Repository;
import org.example.github2.Entity.User;
import org.example.github2.Services.DB.UserService;
import org.example.github2.VersionControllerService.Entity.RepositoryTree;
import org.example.github2.VersionControllerService.Models.*;
import org.example.github2.VersionControllerService.Repositories.GitRepRepository;
import org.example.github2.VersionControllerService.Service.ServiceRepositoryTree;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TestHelper {
    private final GitRepRepository gitRepRepository;
    private final ServiceRepositoryTree serviceRepositoryTree;

    public TestHelper(GitRepRepository gitRepRepository, ServiceRepositoryTree serviceRepositoryTree) {
        this.gitRepRepository = gitRepRepository;
        this.serviceRepositoryTree = serviceRepositoryTree;
        clearBd();
    }

    public void newTreeRepositories(int count) {
        for (int i = 0; i < count; i++) {
            File file = new File("/first");
            File file2 = new File("/second");
            List<File> files = new ArrayList<>();
            files.add(file);
            files.add(file2);
            Directory directory = new Directory("1", files, new ArrayList<>());
            Directory directory2 = new Directory("2", files, new ArrayList<>());
            directory.addDirectory(new Directory("3",new ArrayList<>(),new ArrayList<>()));
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
