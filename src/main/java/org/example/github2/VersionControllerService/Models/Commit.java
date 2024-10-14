package org.example.github2.VersionControllerService.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Commit {
    private Commit nextCommit;
    private List<Change> changes = new ArrayList<>();

    public void addChange(Change change){
        changes.add(change);
    }
}
