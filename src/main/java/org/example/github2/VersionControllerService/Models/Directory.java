package org.example.github2.VersionControllerService.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Directory {
    private String name;
    private List<File> files = new ArrayList<>();
    private List<Directory> directories = new ArrayList<>();
    private boolean isDelete = false;

    public Directory(String name, List<File> files, List<Directory> directories) {
        this.name = name;
        this.files = files;
        this.directories = directories;
    }

    public Directory(String name) {
        this.name = name;
    }

    public void addDirectory(Directory directory){
        directories.add(directory);
    }

    public void addFile(File file){
        files.add(file);
    }
}
