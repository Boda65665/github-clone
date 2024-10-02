package org.example.github2.VersionControllerService.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Delta {
    private int startLine;
    private int endLine;
    private String content;
    private String pathFile;
    private Action action;
}
