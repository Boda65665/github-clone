package org.example.github2.RequestBody;

import lombok.Data;
import org.example.github2.VersionControllerService.Models.Change;

import java.util.List;

@Data
public class CommitBody {
    private String name;
    private List<Change> changes;
}
