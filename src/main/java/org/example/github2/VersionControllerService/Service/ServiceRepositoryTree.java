package org.example.github2.VersionControllerService.Service;

import org.example.github2.Entity.Repository;
import org.example.github2.Entity.User;
import org.example.github2.Model.SourceType;
import org.example.github2.Repositoryes.RepositoryRepository;
import org.example.github2.VersionControllerService.Entity.RepositoryTree;
import org.example.github2.VersionControllerService.Models.*;
import org.example.github2.VersionControllerService.Repositories.GitRepRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class ServiceRepositoryTree {
    private final GitRepRepository gitRepRepository;
    private final MongoTemplate mongoTemplate;
    private final RepositoryRepository repositoryRepository;
    @Value("${name.disk.with.repository}")
    private String NAME_DISK_WITH_REPOSITORY;

    public ServiceRepositoryTree(GitRepRepository gitRepRepository, MongoTemplate mongoTemplate, RepositoryRepository repositoryRepository) {
        this.gitRepRepository = gitRepRepository;
        this.mongoTemplate = mongoTemplate;
        this.repositoryRepository = repositoryRepository;
    }

    public void addNewCommit(Commit commit, int idRepository) {
        RepositoryTree repositoryTree = findById(idRepository);
        repositoryTree.addCommit(commit);
        update(repositoryTree);
    }

    public RepositoryTree findById(int id) {
        return gitRepRepository.findByRepositoryId(id);
    }

    public void update(RepositoryTree updateRepositoryTree) {
        Update update = new Update();
        update.set("files", updateRepositoryTree.getFiles());
        update.set("directories", updateRepositoryTree.getDirectories());
        update.set("commit", updateRepositoryTree.getCommit());
        update.set("isDelete", updateRepositoryTree.isDelete());
        mongoTemplate.findAndModify(
                query(where("repositoryId").is(updateRepositoryTree.getRepositoryId())),
                update,
                RepositoryTree.class
        );
    }

    public void save(RepositoryTree newRepositoryTree) {
        gitRepRepository.save(newRepositoryTree);
    }

    public void addNewFile(String basePath, MultipartFile[] files, int repositoryId) throws IOException {
        Repository repository = repositoryRepository.findById(repositoryId);
        User owner = repository.getOwner();
        String pathDirectoryInTree = basePath.replace("/repository/" + owner.getLogin() + "/" + repository.getName(), "");
        for (MultipartFile file : files) {
            addNewFile(new File(file.getOriginalFilename()), pathDirectoryInTree, repository.getId());
            Path path = Path.of(NAME_DISK_WITH_REPOSITORY + basePath + "/" + file.getOriginalFilename());
            Files.write(path, file.getBytes());
        }
    }

    public void addNewFile(File file, String path, int idRepositoryTree) {
        addNewCommit(path+"/"+file.getName(), idRepositoryTree, Action.ADD_FILE);
        RepositoryTree repositoryTree = gitRepRepository.findByRepositoryId(idRepositoryTree);
        String[] namesDirectory = path.split("/");
        if (path.equals("/") || path.isEmpty()) {
            repositoryTree.addFile(file);
        } else {
            Directory directory = gerDirectory(repositoryTree, namesDirectory);
            directory.addFile(file);
        }
        update(repositoryTree);
    }


    private void addNewCommit(String path, int idRepositoryTree, Action action) {
        RepositoryTree repositoryTree = gitRepRepository.findByRepositoryId(idRepositoryTree);
        Change change = new Change(action, path);
        Commit commit = new Commit();
        commit.addChange(change);
        repositoryTree.addCommit(commit);
        update(repositoryTree);
    }

    private Directory gerDirectory(RepositoryTree repositoryTree, String[] namesDirectory) {
        List<Directory> directories = repositoryTree.getDirectories();
        outerLoop:
        for (int i = 1; i < namesDirectory.length; i++) {
            if (!directories.isEmpty()) {
                String nameDirectory = namesDirectory[i];
                for (int j = 0; j < directories.size(); j++) {
                    Directory directory = directories.get(j);

                    if (nameDirectory.equals(directory.getName())) {
                        if (namesDirectory.length - 1 == i) {
                            return directory;
                        } else {
                            directories = directory.getDirectories();
                            continue outerLoop;
                        }
                    }
                }
            }
            namesDirectory = Arrays.copyOfRange(namesDirectory, i, namesDirectory.length);
            return addMissingDirectory(namesDirectory, directories);
        }
        return null;
    }

    private Directory addMissingDirectory(String[] namesDirectory, List<Directory> directories) {
        for (int i = 0; i < namesDirectory.length; i++) {
            Directory directory = new Directory(namesDirectory[i], new ArrayList<>(), new ArrayList<>());
            directories.add(directory);
            directories = directory.getDirectories();
            if (i == namesDirectory.length - 1) {
                return directory;
            }
        }
        return null;
    }

    public void deleteFile(String path, int idRepositoryTree) {
        RepositoryTree repositoryTree = gitRepRepository.findByRepositoryId(idRepositoryTree);
        File file = getFileByPath(path, repositoryTree);
        if (file == null) return;
        file.setDelete(true);
        update(repositoryTree);
        addNewCommit(path, idRepositoryTree, Action.DELETE_FILE);
    }

    public File getFileByPath(String path, RepositoryTree repositoryTree) {
        String[] namesDirectory = path.split("/");
        String nameFile = namesDirectory[namesDirectory.length - 1];
        if (namesDirectory.length == 2) return findFile(nameFile, repositoryTree.getFiles());
        namesDirectory = Arrays.copyOf(namesDirectory, namesDirectory.length - 1);
        Directory directory = gerDirectory(repositoryTree, namesDirectory);
        return findFile(nameFile, directory.getFiles());
    }

    private File findFile(String nameFile, List<File> files) {
        for (File file : files) {
            if (file.getName().equals(nameFile)) {
                return file;
            }
        }
        return null;
    }

    public void deleteDirectory(String path, int idRepositoryTree) {
        RepositoryTree repositoryTree = gitRepRepository.findByRepositoryId(idRepositoryTree);
        String[] namesDirectory = path.split("/");
        Directory directory = gerDirectory(repositoryTree, namesDirectory);
        directory.setDelete(true);
        update(repositoryTree);
        addNewCommit(path, idRepositoryTree, Action.DELETE_DIRECTORY);
    }

    public boolean isDeleteSource(int idRepository, String pathString, SourceType sourceType) {
        RepositoryTree repositoryTree = gitRepRepository.findByRepositoryId(idRepository);
        if (repositoryTree.isDelete()) return true;
        if (pathString.isEmpty()) return false;
        String[] repositoryNames = pathString.split("/");
        List<Directory> directories = repositoryTree.getDirectories();
        List<File> files = repositoryTree.getFiles();
        for (int i = 0; i < repositoryNames.length; i++) {
            if (i == repositoryNames.length - 1) {
                if (sourceType == SourceType.DIRECTORY) {
                    if (isDeleteDirectory(directories,repositoryNames[repositoryNames.length - 1])) return true;
                } else {
                    if(isDeleteFile(files,repositoryNames[repositoryNames.length - 1])) return true;
                }
            }
            for (Directory directory : directories) {
                if (directory.getName().equals(repositoryNames[i])) {
                    if (directory.isDelete()) return true;
                    directories = directory.getDirectories();
                    files = directory.getFiles();
                    break;
                }
            }
        }
        return getFileByPath(pathString, repositoryTree).isDelete();
    }

    private boolean isDeleteFile(List<File> files, String repositoryName) {
        for (File file : files) {
            if (file.getName().equals(repositoryName)) return file.isDelete();
        }
        return false;
    }

    private boolean isDeleteDirectory(List<Directory> directories, String repositoryName) {
        for (Directory directory : directories) {
            if (directory.getName().equals(repositoryName))
                return directory.isDelete();
        }
        return false;
    }

    public Directory getDirectoryByPath(int ideRepository, String path) {
        RepositoryTree repositoryTree = gitRepRepository.findByRepositoryId(ideRepository);
        return getDirectoryByPath(repositoryTree, path);
    }

    public Directory getDirectoryByPath(RepositoryTree repositoryTree, String path) {
        if (path.isEmpty()) return new Directory("root", repositoryTree.getFiles(), repositoryTree.getDirectories());
        String[] repositoryNames = path.split("/");
        return gerDirectory(repositoryTree, repositoryNames);
    }

    public void deleteRepository(int id) {
        RepositoryTree repositoryTree = gitRepRepository.findByRepositoryId(id);
        repositoryTree.setDelete(true);
        update(repositoryTree);
    }

    public boolean isDeleteRepository(int id) {
        RepositoryTree repositoryTree = gitRepRepository.findByRepositoryId(id);
        return repositoryTree.isDelete();
    }

    public void addNewDirectory(String pathDirectory, int idRep) {
        addDirectoryToStorage(pathDirectory);
        addNewCommit(pathDirectory, idRep, Action.ADD_DIRECTORY);
        String[] namesDirectory = pathDirectory.split("/");
        RepositoryTree repositoryTree = gitRepRepository.findByRepositoryId(idRep);
        if (namesDirectory.length == 1) {
            repositoryTree.addDirectory(new Directory(namesDirectory[0]));
        } else {
            gerDirectory(repositoryTree, namesDirectory);
        }
        update(repositoryTree);
    }


    private void addDirectoryToStorage(String pathDirectory) {
        Path path = Paths.get(pathDirectory);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


