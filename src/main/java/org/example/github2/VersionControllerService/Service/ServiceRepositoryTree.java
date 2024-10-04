package org.example.github2.VersionControllerService.Service;

import org.example.github2.VersionControllerService.Entity.RepositoryTree;
import org.example.github2.VersionControllerService.Models.Commit;
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

    public void addNewCommit(String pathToDirectory,String nameFile, Commit commit, int idRepository){
        RepositoryTree repositoryTree = findById(idRepository);
        File file = findFileByPath(pathToDirectory, nameFile, repositoryTree);
        if (file == null) return;
        file.addCommit(commit);
        update(repositoryTree);
    }

    private File findFileByPath(String pathToDirectory,String nameFile, RepositoryTree repositoryTree) {
        List<File> files = new ArrayList<>();
        if (pathToDirectory.equals("/")) {
            files = repositoryTree.getFiles();
        }
        else {
            Directory directory = getDirectoryByPath(repositoryTree,pathToDirectory);
            if (directory==null) return null;
            files = directory.getFiles();
        }
        for (File file : files) {
            if (file.getPathOriginalFile().equals("/"+nameFile)){
                return file;
            }
        }
        return null;
    }

    private Directory getDirectoryByPath(RepositoryTree repositoryTree, String pathToDirectory) {
        String[] nameDirectories = pathToDirectory.split("/");
        List<Directory> directories = repositoryTree.getDirectories();
        for (int i = 1;i<nameDirectories.length;i++) {
            boolean findDirectory = false;
            for (Directory directory : directories) {
                if (nameDirectories[i].equals(directory.getName())){
                    directories=directory.getDirectories();
                    findDirectory = true;
                    if(i==nameDirectories.length-1) return directory;
                    break;
                }
            }
            if (!findDirectory) return null;
        }
        return null;
    }

    private File findFile(List<File> files, String path) {
        for (File file : files) {
            if (file.getPathOriginalFile().equals(path)) {
                return file;
            }
        }
        return null;
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

    public RepositoryTree findById(int id) {
        return gitRepRepository.findByRepositoryId(id);
    }

    public Commit getCommitByPathToFile(String pathDirectory, String nameFile, int idRepository) {
        File file = findFileByPath(pathDirectory, nameFile, gitRepRepository.findByRepositoryId(idRepository));
        if (file==null) return null;
        return file.getCommit();
    }
}
