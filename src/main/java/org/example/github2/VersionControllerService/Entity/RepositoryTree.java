package org.example.github2.VersionControllerService.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.github2.VersionControllerService.Models.Commit;
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
    private Commit commit;
    private List<Directory> directories = new ArrayList<>();
    private boolean isDelete;
    @Id
    private int repositoryId;

    public RepositoryTree(List<File> files, List<Directory> directories, int repositoryId) {
        this.files = files;
        this.directories = directories;
        this.repositoryId = repositoryId;
    }

    public RepositoryTree(int repositoryId) {
        this.repositoryId = repositoryId;
    }

    public void addFile(File file){
        files.add(file);
    }

    public void addDirectory(Directory directory){
        directories.add(directory);
    }

    public void addCommit(Commit commit){
        if (this.commit!=null) commit.setNextCommit(this.commit);
        this.commit = commit;
    }
}
