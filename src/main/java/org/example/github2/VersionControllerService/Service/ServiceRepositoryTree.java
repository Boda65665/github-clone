package org.example.github2.VersionControllerService.Service;

import org.example.github2.VersionControllerService.Entity.RepositoryTree;
import org.example.github2.VersionControllerService.Models.Directory;
import org.example.github2.VersionControllerService.Models.File;
import org.example.github2.VersionControllerService.Repositories.GitRepRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class ServiceRepositoryTree {
    private final GitRepRepository gitRepRepository;
    private final MongoTemplate mongoTemplate;

    public ServiceRepositoryTree(GitRepRepository gitRepRepository, MongoTemplate mongoTemplate) {
        this.gitRepRepository = gitRepRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public void save(RepositoryTree newRepositoryTree){
        gitRepRepository.save(newRepositoryTree);
    }

    public void update(RepositoryTree updateRepositoryTree){
        Update update = new Update();
        update.set("files", updateRepositoryTree.getFiles());
        update.set("directories", updateRepositoryTree.getDirectories());
        mongoTemplate.findAndModify(
                query(where("repositoryId").is(updateRepositoryTree.getRepositoryId())),
                update,
                RepositoryTree.class
        );
    }

    public void addNewFile(File file,String path,int idRepositoryTree){
        RepositoryTree repositoryTree = gitRepRepository.findByRepositoryId(idRepositoryTree);
        String[] namesDirectory = path.split("/");
        if (path.equals("/") || path.isEmpty()){
            repositoryTree.addFile(file);
        }
        List<Directory> directories = repositoryTree.getDirectories();
        for (int i = 1; i < namesDirectory.length; i++) {
            if (directories.isEmpty()) {
                for (int g = i; g < namesDirectory.length; g++) {
                    Directory directory = new Directory(namesDirectory[g], new ArrayList<>(), new ArrayList<>());
                    directories.add(directory);
                    directories = directory.getDirectories();
                    if (g == namesDirectory.length-1){
                        directory.addFile(file);
                    }
                }
                break;
            } else {
                String nameDirectory = namesDirectory[i];
                for (int j = 0; j < directories.size(); j++) {
                    Directory directory = directories.get(j);
                    if (nameDirectory.equals(directory.getName())) {
                        if (namesDirectory.length - 1 == i) {
                            directory.addFile(file);
                        } else {
                            directories = directory.getDirectories();
                        }
                    }
                }
            }
        }
        update(repositoryTree);
    }

    public RepositoryTree findById(int id) {
        return gitRepRepository.findByRepositoryId(id);
    }
}
