package org.example.github2.VersionControllerService.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class Change {
    private int numberLine;
    private String location;
    private Action action;
    private String content;

    public Change(Action action, String location) {
        this.action = action;
        this.location = location;
    }

    public Change(Action action, String location, String content, int numberLine) {
        this.action = action;
        this.location = location;
        this.content = content;
        this.numberLine = numberLine;
    }
}
