package org.example.github2.VersionControllerService.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class Change {
    private int numberLine;
    private Action action;
    private String content;

    public Change(Action action, String content) {
        this.action = action;
        this.content = content;
    }
}
