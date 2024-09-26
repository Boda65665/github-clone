package org.example.github2.VersionControllerService.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Commit {
    private Commit nextCommit;
    private Change change;
}
