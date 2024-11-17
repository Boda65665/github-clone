package org.example.github2.VersionControllerService.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class File {
    private String name;
    private boolean isDelete;

    public File(String name) {
        this.name = name;
    }
}
