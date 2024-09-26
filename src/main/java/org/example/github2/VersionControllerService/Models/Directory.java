package org.example.github2.VersionControllerService.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Directory {
    private String name;
    private List<File> files;
    private List<Directory> directories;
}
