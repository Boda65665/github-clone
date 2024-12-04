package org.example.github2.Repository.Services.CommitService;

import org.example.github2.Repository.TestHelper;
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
    private TestHelper testHelper;
    @Autowired
    private ServiceRepositoryTree serviceRepositoryTree;
    private final String currentDir = System.getProperty("user.dir");
    private final String PATH_TO_FILES = currentDir+"\\src\\test\\java\\org\\example\\github2\\Repository\\Services\\CommitService\\testFiles";

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestHelper testAssistant(GitRepRepository gitRepRepository, ServiceRepositoryTree serviceRepositoryTree) {
            return new TestHelper(gitRepRepository,serviceRepositoryTree);
        }
    }

    @Test
    void addNewCommit() throws IOException {
        String basePath = PATH_TO_FILES + "\\filesForTestAddCommit";
        String pathToNewContent = basePath + "\\newContent";
        String pathToInitialMeaning = basePath + "\\origContent";
        setTestFileInitialMeaning(basePath+"\\testFile" ,pathToInitialMeaning);
        testHelper.newTreeRepositories(1);

        String newContent = String.join("\n", Files.readAllLines(Path.of(pathToNewContent)));
        commitService.addNewCommit(basePath,"testFile", newContent, 0);
        testAddingCommit();
    }

    private void setTestFileInitialMeaning(String pathToTestFile, String pathToInitialMeaning) throws IOException {
        String newContent = String.join("\n", Files.readAllLines(Path.of(pathToInitialMeaning)));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathToTestFile))) {
            writer.write(newContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testAddingCommit() {
        RepositoryTree repositoryTree = serviceRepositoryTree.findById(0);
        Commit commit = repositoryTree.getCommit();
        List<Change> changes = commit.getChanges();
        Change firstChange = changes.getFirst();
        Assertions.assertEquals(Action.EDIT_CONTENT_IN_FILE, firstChange.getAction());
        Change lastChange = changes.getLast();
        Assertions.assertEquals(Action.DELETE_CONTENT_IN_FILE, lastChange.getAction());

    }

    @Test
    void convert() {
        List<Delta> deltas = generateTestDelta();
        Commit commit = commitService.getCommit(deltas);
        List<Change> changes = commit.getChanges();
        Assertions.assertEquals(2, changes.size());
    }

    private List<Delta> generateTestDelta() {
        List<Delta> newDeltas = new ArrayList<>();
        newDeltas.add(new Delta(1,"fwew","/a", Action.EDIT_CONTENT_IN_FILE));
        newDeltas.add(new Delta(4,"dqq","/a",Action.ADD_CONTENT_IN_FILE));
        return newDeltas;
    }

    @Test
    void modifiedFileToCommit() throws IOException {
        String basePath = PATH_TO_FILES + "\\filesForTestGetCommit";
        List<String> editFileActionInsert = Files.readAllLines(Path.of(basePath+"\\editFileActionInsert"));
        List<String> editFileActionEdit = Files.readAllLines(Path.of(basePath+"\\editFileActionEdit"));
        List<String> editFileActionDelete = Files.readAllLines(Path.of(basePath+"\\editFileActionDelete"));

        String pathToOriginalFile = basePath+"\\originalTestFile";
        Commit commitActionInsert = getCommit(pathToOriginalFile , editFileActionInsert);
        testGetCommitFromFile(commitActionInsert,Action.ADD_CONTENT_IN_FILE,"regregrgeggreer\nergregregregrer",8,0);

        Commit commitActionEdit = getCommit(pathToOriginalFile, editFileActionEdit);

        testGetCommitFromFile(commitActionEdit,Action.EDIT_CONTENT_IN_FILE,"1~wefewfewfewfef~fewewffffffffffffffffff",1,0);
        testGetCommitFromFile(commitActionEdit,Action.EDIT_CONTENT_IN_FILE,"2~wfegfewfewfefwefef~wfdewfewfefwefewfewfewf\nefwfewfewfewfewfewfefewffefewfe",7,1);


        Commit commitActionDelete = getCommit(pathToOriginalFile, editFileActionDelete);
        testGetCommitFromFile(commitActionDelete,Action.DELETE_CONTENT_IN_FILE,"f",10,0);
    }

    private Commit getCommit(String pathOrig, List<String> editFile) throws IOException {
        return commitService.getCommit(pathOrig, String.join("\n",editFile));
    }

    private void testGetCommitFromFile(Commit commit,Action action,String exceptedContent,int startIndexChangingLine,int indexTestingChange){
        List<Change> changes = commit.getChanges();
        Change change = changes.get(indexTestingChange);
        Assertions.assertEquals(exceptedContent, change.getContent());
        Assertions.assertEquals(startIndexChangingLine,change.getNumberLine());
        Assertions.assertEquals(action, change.getAction());
    }
}
