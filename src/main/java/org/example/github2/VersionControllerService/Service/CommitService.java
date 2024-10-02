package org.example.github2.VersionControllerService.Service;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import org.example.github2.VersionControllerService.Models.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CommitService {

    public Commit deltaToCommit(List<Delta> deltas) throws IOException {
        Commit commit = new Commit();
        for (Delta delta : deltas) {
            commit.addChange(getChange(delta));
        }
        return commit;
    }

    private Change getChange(Delta delta) {
        Position position = new Position(delta.getStartLine(), delta.getEndLine());
        return new Change(position, delta.getAction(), delta.getContent());
    }

    public Commit deltaToCommit(String originalPathFile, String newContent) throws IOException {
        List<Delta> deltas = getDelta(originalPathFile, newContent);
        return deltaToCommit(deltas);
    }

    private List<Delta> getDelta(String originalPathFile, String newContent) throws IOException {
        List<String> originalContent = Files.readAllLines(Paths.get(originalPathFile));
        List<String> updateContent = List.of(newContent.split("\n"));
        Patch<String> patch = DiffUtils.diff(originalContent, updateContent);
        List<Delta> deltas = new ArrayList<>();
        for (AbstractDelta<String> delta : patch.getDeltas()) {
            int positionStart = delta.getTarget().getPosition();
            int positionEnd = positionStart + delta.getTarget().size();
            Action action = (delta.getType() == DeltaType.DELETE)?Action.DELETE:
                    (delta.getType()==DeltaType.CHANGE)?Action.EDIT:Action.ADD;
            String editLines = "";
            if (action!=Action.DELETE){
                editLines = String.join("\n", updateContent.subList(positionStart,positionEnd));
            }
            else {
                editLines =  String.join("\n", delta.getSource().getLines());
            }
            deltas.add(new Delta(positionStart, positionEnd,editLines,originalPathFile,action));
        }
        return deltas;
    }
}
