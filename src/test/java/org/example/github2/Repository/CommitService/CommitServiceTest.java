package org.example.github2.Repository.CommitService;

import org.checkerframework.checker.units.qual.C;
import org.example.github2.Repository.TestAssistant;
import org.example.github2.Repositoryes.RepositoryRepository;
import org.example.github2.VersionControllerService.Entity.RepositoryTree;
import org.example.github2.VersionControllerService.Models.*;
import org.example.github2.VersionControllerService.Repositories.GitRepRepository;
import org.example.github2.VersionControllerService.Service.CommitService;
import org.example.github2.VersionControllerService.Service.ServiceRepositoryTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class CommitServiceTest {
    @Autowired
    private CommitService commitService;
    @Autowired
    private TestAssistant testAssistant;
    @Autowired
    private ServiceRepositoryTree serviceRepositoryTree;


    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestAssistant testAssistant(GitRepRepository gitRepRepository,ServiceRepositoryTree serviceRepositoryTree) {
            return new TestAssistant(gitRepRepository,serviceRepositoryTree);
        }
    }

    @Test
    void addNewCommit() throws IOException {
        String basePath = "P:\\dataForTest\\nameRep\\NameUser\\filesForTestAddCommit";
        String pathToTestFile = basePath + "\\testFile";
        String pathToNewContent = basePath + "\\newContent";
        String pathToInitialMeaning = basePath + "\\origContent";
        setTestFileInitialMeaning(pathToTestFile, pathToInitialMeaning);
        testAssistant.newRepositories(1);

        File file = new File("/testFile");
        String pathFileInTree = basePath.replace("P:\\dataForTest\\nameRep\\NameUser\\","/");
        serviceRepositoryTree.addNewFile(file,pathFileInTree,0);

        String newContent = String.join("\n", Files.readAllLines(Path.of(pathToNewContent)));
        commitService.addNewCommit(basePath,"testFile", newContent, 0);
        testSetNewContentFile(pathToTestFile, pathToNewContent);
        testAddingCommitToFile();
    }

    private void setTestFileInitialMeaning(String pathToTestFile, String pathToInitialMeaning) throws IOException {
        String newContent = String.join("\n", Files.readAllLines(Path.of(pathToInitialMeaning)));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathToTestFile))) {
            writer.write(newContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testAddingCommitToFile() {
        RepositoryTree repositoryTree = serviceRepositoryTree.findById(0);
        Commit commit = repositoryTree.getCommit();
        List<Change> changes = commit.getChanges();
        Change firstChange = changes.getFirst();
        Assertions.assertEquals(Action.EDIT_CONTENT_IN_FILE, firstChange.getAction());
        Change lastChange = changes.getLast();
        Assertions.assertEquals(Action.DELETE_CONTENT_IN_FILE, lastChange.getAction());

    }


    private void testSetNewContentFile(String pathToTestFile, String pathToNewContent) throws IOException {
        List<String> testFile = Files.readAllLines(Path.of(pathToTestFile));
        List<String> newContent = Files.readAllLines(Path.of(pathToNewContent));
        Assertions.assertEquals(newContent, testFile);
    }

    @Test
    void convert() throws IOException {
        List<Delta> deltas = generateTestDelta();
        Commit commit = commitService.getCommit(deltas);
        List<Change> changes = commit.getChanges();
        Assertions.assertEquals(2, changes.size());
    }

    private List<Delta> generateTestDelta() {
        List<Delta> newDeltas = new ArrayList<>();
        newDeltas.add(new Delta(1,2,"fwew","/a", Action.EDIT_CONTENT_IN_FILE));
        newDeltas.add(new Delta(4,4,"dqq","/a",Action.ADD_CONTENT_IN_FILE));
        return newDeltas;
    }

    @Test
    void editFileToCommit() throws IOException {
        String basePath = "P:\\dataForTest\\nameRep\\NameUser\\filesForTestGetCommit";
        List<String> editFileActionInsert = Files.readAllLines(Path.of(basePath+"\\editFileActionInsert"));
        List<String> editFileActionEdit = Files.readAllLines(Path.of(basePath+"\\editFileActionEdit"));
        List<String> editFileActionDelete = Files.readAllLines(Path.of(basePath+"\\editFileActionDelete"));

        Commit commitActionInsert = getCommit(basePath+"\\originalTestFile", editFileActionInsert);
        testGetCommitFromFileActionInsert(commitActionInsert);

        Commit commitActionEdit = getCommit(basePath+"\\originalTestFile", editFileActionEdit);
        testGetCommitFromFileActionEdit(commitActionEdit);

        Commit commitActionDelete = getCommit(basePath+"\\originalTestFile", editFileActionDelete);
        testGetCommitFromFileActionDelete(commitActionDelete);
    }

    private Commit getCommit(String pathOrig, List<String> editFile) throws IOException {
        return commitService.getCommit(pathOrig, String.join("\n",editFile));
    }

    private void testGetCommitFromFileActionInsert(Commit commit) {
        List<Change> changes = commit.getChanges();
        Change change = changes.getFirst();
        Assertions.assertEquals("fweeeeeeeeeeewewefewferfef", change.getContent());
        Assertions.assertEquals(5,change.getPosition().getLineStart());
        Assertions.assertEquals(6,change.getPosition().getLineEnd());
        Assertions.assertEquals(Action.ADD_CONTENT_IN_FILE, change.getAction());
    }

    private void testGetCommitFromFileActionEdit(Commit commit) {
        List<Change> changes = commit.getChanges();
        Change changeOnOneLine = changes.getFirst();
        Assertions.assertEquals("test 34324s43243", changeOnOneLine.getContent());
        Assertions.assertEquals(0,changeOnOneLine.getPosition().getLineStart());
        Assertions.assertEquals(1,changeOnOneLine.getPosition().getLineEnd());
        Assertions.assertEquals(Action.EDIT_CONTENT_IN_FILE, changeOnOneLine.getAction());

        Change changeOnTwoLines = changes.get(1);
        Assertions.assertEquals("test123421434wwee\nd", changeOnTwoLines.getContent());
        Assertions.assertEquals(3,changeOnTwoLines.getPosition().getLineStart());
        Assertions.assertEquals(5,changeOnTwoLines.getPosition().getLineEnd());
        Assertions.assertEquals(Action.EDIT_CONTENT_IN_FILE, changeOnTwoLines.getAction());
    }

    private void testGetCommitFromFileActionDelete(Commit commit) {
        List<Change> changes = commit.getChanges();
        Change change = changes.getFirst();
        Assertions.assertEquals("test3211111111111111111111111", change.getContent());
        Assertions.assertEquals(4,change.getPosition().getLineStart());
        Assertions.assertEquals(4,change.getPosition().getLineEnd());
        Assertions.assertEquals(Action.DELETE_CONTENT_IN_FILE, change.getAction());
    }
}
