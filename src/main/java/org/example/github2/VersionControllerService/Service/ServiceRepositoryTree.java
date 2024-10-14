package org.example.github2.VersionControllerService.Service;

import org.example.github2.Entity.Repository;
import org.example.github2.Entity.User;
import org.example.github2.Repositoryes.RepositoryRepository;
import org.example.github2.VersionControllerService.Entity.RepositoryTree;
import org.example.github2.VersionControllerService.Models.*;
import org.example.github2.VersionControllerService.Repositories.GitRepRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class ServiceRepositoryTree {
    private final GitRepRepository gitRepRepository;
    private final MongoTemplate mongoTemplate;
    private final RepositoryRepository repositoryRepository;

    public ServiceRepositoryTree(GitRepRepository gitRepRepository, MongoTemplate mongoTemplate, RepositoryRepository repositoryRepository) {
        this.gitRepRepository = gitRepRepository;
        this.mongoTemplate = mongoTemplate;
        this.repositoryRepository = repositoryRepository;
    }

    public void addNewCommit(Commit commit, int idRepository){
        RepositoryTree repositoryTree = findById(idRepository);
        repositoryTree.addCommit(commit);
        update(repositoryTree);
    }

    public RepositoryTree findById(int id) {
        return gitRepRepository.findByRepositoryId(id);
    }

    public void update(RepositoryTree updateRepositoryTree){
        Update update = new Update();
        update.set("files", updateRepositoryTree.getFiles());
        update.set("directories", updateRepositoryTree.getDirectories());
        update.set("commit", updateRepositoryTree.getCommit());
        mongoTemplate.findAndModify(
                query(where("repositoryId").is(updateRepositoryTree.getRepositoryId())),
                update,
                RepositoryTree.class
        );
    }

    public void save(RepositoryTree newRepositoryTree){
        gitRepRepository.save(newRepositoryTree);
    }

    public void addNewFile(String basePath, MultipartFile[] files,int repositoryId) throws IOException {
        Repository repository = repositoryRepository.findById(repositoryId);
        User owner = repository.getOwner();
        String pathDirectoryInTree = basePath.replace("/repository/"+owner.getLogin()+"/"+repository.getName(),"");
        for (MultipartFile file : files) {
            addNewFile(new org.example.github2.VersionControllerService.Models.File("/"+file.getOriginalFilename()),
                    pathDirectoryInTree,repository.getId());
            Path path = Path.of("P:"+basePath+"/"+file.getOriginalFilename());
            Files.write(path, file.getBytes());
        }
    }
    
    public void addNewFile(File file,String path,int idRepositoryTree){
        addNewCommit(path,idRepositoryTree, Action.ADD_FILE);
        RepositoryTree repositoryTree = gitRepRepository.findByRepositoryId(idRepositoryTree);
        String[] namesDirectory = path.split("/");
        if (path.equals("/") || path.isEmpty()){
            repositoryTree.addFile(file);
        }
        List<Directory> directories = repositoryTree.getDirectories();
        outerLoop:
        for (int i = 1; i < namesDirectory.length; i++) {
            boolean findDirectory = false;
            if (!directories.isEmpty()) {
                String nameDirectory = namesDirectory[i];
                for (int j = 0; j < directories.size(); j++) {
                    Directory directory = directories.get(j);
                    if (nameDirectory.equals(directory.getName())) {
                        findDirectory = true;
                        if (namesDirectory.length - 1 == i) {
                            directory.addFile(file);
                        } else {
                            directories = directory.getDirectories();
                            continue outerLoop;
                        }
                    }
                }
            }
            if (directories.isEmpty() || !findDirectory) {
                for (int g = i; g < namesDirectory.length; g++) {
                    Directory directory = new Directory(namesDirectory[g], new ArrayList<>(), new ArrayList<>());
                    directories.add(directory);
                    directories = directory.getDirectories();
                    if (g == namesDirectory.length-1){
                        directory.addFile(file);
                    }
                }
                break;
            }
        }
        update(repositoryTree);
    }

    private void addNewCommit(String path, int idRepositoryTree, Action action) {
        RepositoryTree repositoryTree = gitRepRepository.findByRepositoryId(idRepositoryTree);
        Change change = new Change(action, (path.isEmpty())?"/":path);
        Commit commit = new Commit();
        commit.addChange(change);
        repositoryTree.addCommit(commit);
        update(repositoryTree);
    }

    public void deleteFile(String path, int idRepositoryTree){
        addNewCommit(path, idRepositoryTree, Action.DELETE_FILE);
    }
}



