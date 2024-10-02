package org.example.github2.Repository.CommitService;

import org.example.github2.VersionControllerService.Models.Action;
import org.example.github2.VersionControllerService.Models.Change;
import org.example.github2.VersionControllerService.Models.Commit;
import org.example.github2.VersionControllerService.Models.Delta;
import org.example.github2.VersionControllerService.Service.CommitService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CommitServiceTest {
    private final CommitService commitService = new CommitService();

    @Test
    void convert() throws IOException {
        List<Delta> deltas = generateTestDelta();
        Commit commit = commitService.deltaToCommit(deltas);
        List<Change> changes = commit.getChanges();
        Assertions.assertEquals(2, changes.size());
    }

    private List<Delta> generateTestDelta() {
        List<Delta> newDeltas = new ArrayList<>();
        newDeltas.add(new Delta(1,2,"fwew","/a", Action.EDIT));
        newDeltas.add(new Delta(4,4,"dqq","/a",Action.ADD));
        return newDeltas;
    }

    @Test
    void editFileToCommit() throws IOException {
        String basePath = "P:\\github2\\src\\test\\java\\org\\example\\github2\\Repository\\CommitService";
        List<String> editFileActionInsert = Files.readAllLines(Path.of(basePath+"\\editFileActionInsert"));
        List<String> editFileActionEdit = Files.readAllLines(Path.of(basePath+"\\editFileActionEdit"));
        List<String> editFileActionDelete = Files.readAllLines(Path.of(basePath+"\\editFileActionDelete"));

        Commit commitActionInsert = getDeltas(basePath+"\\originalTestFile", editFileActionInsert);
        testGetCommitFromFileActionInsert(commitActionInsert);

        Commit commitActionEdit = getDeltas(basePath+"\\originalTestFile", editFileActionEdit);
        testGetCommitFromFileActionEdit(commitActionEdit);

        Commit commitActionDelete = getDeltas(basePath+"\\originalTestFile", editFileActionDelete);
        testGetCommitFromFileActionDelete(commitActionDelete);
    }

    private Commit getDeltas(String pathOrig, List<String> editFile) throws IOException {
        return commitService.deltaToCommit(pathOrig, String.join("\n",editFile));
    }

    private void testGetCommitFromFileActionInsert(Commit commit) {
        List<Change> changes = commit.getChanges();
        Change change = changes.getFirst();
        Assertions.assertEquals("fweeeeeeeeeeewewefewferfef", change.getContent());
        Assertions.assertEquals(5,change.getPosition().getLineStart());
        Assertions.assertEquals(6,change.getPosition().getLineEnd());
        Assertions.assertEquals(Action.ADD, change.getAction());
    }

    private void testGetCommitFromFileActionEdit(Commit commit) {
        List<Change> changes = commit.getChanges();
        Change changeOnOneLine = changes.getFirst();
        Assertions.assertEquals("test 34324s43243", changeOnOneLine.getContent());
        Assertions.assertEquals(0,changeOnOneLine.getPosition().getLineStart());
        Assertions.assertEquals(1,changeOnOneLine.getPosition().getLineEnd());
        Assertions.assertEquals(Action.EDIT, changeOnOneLine.getAction());

        Change changeOnTwoLines = changes.get(1);
        Assertions.assertEquals("test123421434wwee\nd", changeOnTwoLines.getContent());
        Assertions.assertEquals(3,changeOnTwoLines.getPosition().getLineStart());
        Assertions.assertEquals(5,changeOnTwoLines.getPosition().getLineEnd());
        Assertions.assertEquals(Action.EDIT, changeOnTwoLines.getAction());
    }

    private void testGetCommitFromFileActionDelete(Commit commit) {
        List<Change> changes = commit.getChanges();
        Change change = changes.getFirst();
        Assertions.assertEquals("test3211111111111111111111111", change.getContent());
        Assertions.assertEquals(4,change.getPosition().getLineStart());
        Assertions.assertEquals(4,change.getPosition().getLineEnd());
        Assertions.assertEquals(Action.DELETE, change.getAction());
    }
}
