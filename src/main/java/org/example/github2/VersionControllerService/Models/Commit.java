package org.example.github2.VersionControllerService.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.github2.VersionControllerService.Service.CommitService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Commit {
    private Commit nextCommit;
    private List<Change> changes = new ArrayList<>();
    private String hashId = CommitService.generateHashId();
    private String name = LocalDateTime.now().toString();
    private boolean isCanceled = false;

    public void addChange(Change change){
        changes.add(change);
    }

    public Commit(Commit nextCommit, List<Change> changes, String hashId, String name) {
        this.nextCommit = nextCommit;
        this.changes = changes;
        this.hashId = hashId;
        this.name = name;
    }
}
