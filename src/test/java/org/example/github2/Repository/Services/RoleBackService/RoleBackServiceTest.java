package org.example.github2.Repository.Services.RoleBackService;

import org.example.github2.VersionControllerService.Entity.RepositoryTree;
import org.example.github2.VersionControllerService.Models.*;
import org.example.github2.VersionControllerService.Models.File;
import org.example.github2.VersionControllerService.Repositories.GitRepRepository;
import org.example.github2.VersionControllerService.Service.CommitService;
import org.example.github2.VersionControllerService.Service.RoleBackCommitService;
import org.example.github2.VersionControllerService.Service.ServiceRepositoryTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootTest
@ActiveProfiles("test")
public class RoleBackServiceTest {
    @Autowired
    RoleBackCommitService roleBackCommitService;
    @Autowired
    CommitService commitService;
    @Autowired
    TestHelperForRoleBackService testHelperForRoleBackService;
    @Autowired
    ServiceRepositoryTree serviceRepositoryTree;
    @Autowired
    GitRepRepository gitRepRepository;
    private final String currentDir = System.getProperty("user.dir");
    private final String PATH_TO_FILES = currentDir+"\\src\\test\\java\\org\\example\\github2\\Repository\\Services\\RoleBackService\\testFiles";

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestHelperForRoleBackService testAssistant(ServiceRepositoryTree serviceRepositoryTree,GitRepRepository gitRepRepository) {
            return new TestHelperForRoleBackService(serviceRepositoryTree,gitRepRepository);
        }
    }

    @Test
    public void roleBackCommit(){
        testRoleBack(Action.ADD_FILE);
        testRoleBack(Action.ADD_CONTENT_IN_FILE);
        testRoleBack(Action.DELETE_FILE);
        testRoleBack(Action.DELETE_CONTENT_IN_FILE);
        testRoleBack(Action.DELETE_DIRECTORY);
        testRoleBack(Action.ADD_DIRECTORY);
        testRoleBack(Action.EDIT_CONTENT_IN_FILE);
    }


    private void testRoleBack(Action action) {
        clearBd();
        switch (action){
            case ADD_DIRECTORY -> testRbDelOrAddDir(action,true);
            case DELETE_DIRECTORY -> testRbDelOrAddDir(action,false);
            case DELETE_CONTENT_IN_FILE -> testRbDelContentFile();
            case ADD_CONTENT_IN_FILE -> testRbAddContentFile();
            case DELETE_FILE -> testRbDelOrAddFile(Action.DELETE_FILE,false);
            case ADD_FILE -> testRbDelOrAddFile(action,true);
            case EDIT_CONTENT_IN_FILE -> testRbEditContentFile();
        }
    }

    private void testRbDelOrAddDir(Action action, boolean isDelete) {
        testHelperForRoleBackService.createRepositoryWithCommitAction(action);
        roleBackCommitService.roleBackLastCommit(0);
        RepositoryTree repositoryTree = serviceRepositoryTree.findById(0);
        Assertions.assertNotNull(repositoryTree.getCommit());
        Assertions.assertNotNull(repositoryTree.getCommit().getNextCommit());
        Directory directory = serviceRepositoryTree.getDirectoryByPath(0,"/testName");
        Assertions.assertEquals(isDelete,directory.isDelete());
    }

    private void testRbDelContentFile() {
        String pathToTestFile = PATH_TO_FILES+"/AddContentInFile";
        setTestFileInitialMeaning(pathToTestFile+"/orig",pathToTestFile+"/testFile");
        Change change = generateTDForRbDelContentFile();
        callPrivateFunc("addContentInFile",change,pathToTestFile+"/testFile");
        Assertions.assertTrue(compareFiles(pathToTestFile+"/exceptedResult",pathToTestFile+"/testFile"));
    }

    private Change generateTDForRbDelContentFile() {
        String pathTestFile = "D:\\github\\src\\test\\java\\org\\example\\github2\\Repository\\Services\\RoleBackService\\testFiles\\DeleteContentInFile\\testFile";
        return new Change(5,pathTestFile,Action.DELETE_CONTENT_IN_FILE,"ee\neee");
    }

    private void testRbAddContentFile() {
        String pathToTestFile = PATH_TO_FILES+"/DeleteContentInFile";
        setTestFileInitialMeaning(pathToTestFile+"/orig",pathToTestFile+"/testFile");
        Change change = generateTDForRbAddContentFile();
        callPrivateFunc("deleteContentInFile",change,pathToTestFile+"/testFile");

        Assertions.assertTrue(compareFiles(pathToTestFile+"/exceptedResult",pathToTestFile+"/testFile"));
    }

    private static boolean compareFiles(String filePath1, String filePath2) {
        try (BufferedReader reader1 = new BufferedReader(new FileReader(filePath1));
             BufferedReader reader2 = new BufferedReader(new FileReader(filePath2))) {
            String line1, line2;
            while ((line1 = reader1.readLine()) != null && (line2 = reader2.readLine()) != null) {
                if (!line1.equals(line2)) {
                    return false;
                }
            }
            return reader1.readLine() == null && reader2.readLine() == null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setTestFileInitialMeaning(String pathOrigFile,String pathTestFile){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathTestFile))) {
            String newContent = String.join("\n", Files.readAllLines(Path.of(pathOrigFile)));
            writer.write(newContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Change generateTDForRbAddContentFile() {
        String pathTestFile = "D:\\github\\src\\test\\java\\org\\example\\github2\\Repository\\Services\\RoleBackService\\testFiles\\DeleteContentInFile\\testFile";
        return new Change(3,pathTestFile,Action.ADD_CONTENT_IN_FILE,"saaaad\ndddddd");
    }

    private void callPrivateFunc(String nameFunc,Object... objects) {
        try {
            Class<?>[] parameterTypes = new Class[objects.length];
            for (int i = 0; i < objects.length; i++) {
                parameterTypes[i] = objects[i].getClass();
            }
            Method method = RoleBackCommitService.class.getDeclaredMethod(nameFunc,parameterTypes);
            method.setAccessible(true);
            method.invoke(roleBackCommitService,objects);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void testRbDelOrAddFile(Action action, boolean isDelete){
        testHelperForRoleBackService.createRepositoryWithCommitAction(action);
        roleBackCommitService.roleBackLastCommit(0);
        RepositoryTree repositoryTree = serviceRepositoryTree.findById(0);
        Assertions.assertNotNull(repositoryTree.getCommit());
        Assertions.assertNotNull(repositoryTree.getCommit().getNextCommit());
        File file = serviceRepositoryTree.getFileByPath("/testName", repositoryTree);
        Assertions.assertEquals(isDelete,file.isDelete());
    }

    private void testRbEditContentFile() {
        String orig = PATH_TO_FILES + "\\EditContentInFile\\orig";
        String testFile = PATH_TO_FILES + "\\EditContentInFile\\testFile";
        String excepted = PATH_TO_FILES + "\\EditContentInFile\\exceptedResult";
        setTestFileInitialMeaning(orig,testFile);
        callPrivateFunc("removeLines",testFile,2,3);
        callPrivateFunc("insertLines",testFile,"2\n3",1);
        Assertions.assertTrue(compareFiles(testFile,excepted));
    }

    public void clearBd(){
        gitRepRepository.deleteAll();
    }
}
