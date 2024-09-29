package org.example.github2.VersionControllerService.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.github2.VersionControllerService.Models.Directory;
import org.example.github2.VersionControllerService.Models.File;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "repositories")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RepositoryTree {
    private List<File> files = new ArrayList<>();
    private List<Directory> directories = new ArrayList<>();
    @Id
    private int repositoryId;

    public RepositoryTree(int repositoryId) {
        this.repositoryId = repositoryId;
    }

    public void addDirectory(Directory directory){
        directories.add(directory);
    }

    public void addFile(File file){
        files.add(file);
    }
}
