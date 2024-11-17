package org.example.github2.Repository;

import org.example.github2.VersionControllerService.Entity.RepositoryTree;
import org.example.github2.VersionControllerService.Models.Directory;
import org.example.github2.VersionControllerService.Models.File;
import org.example.github2.VersionControllerService.Repositories.GitRepRepository;
import org.example.github2.VersionControllerService.Service.ServiceRepositoryTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class RepositoryTest {
    @Autowired
    private GitRepRepository gitRepRepository;
    @Autowired
    private TestHelper testHelper;
    @Autowired
    private ServiceRepositoryTree serviceRepositoryTree;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestHelper testAssistant(GitRepRepository gitRepRepository, ServiceRepositoryTree serviceRepositoryTree) {
            return new TestHelper(gitRepRepository,serviceRepositoryTree);
        }
    }

    @Test
    @DirtiesContext
    public void saveTest(){
        testHelper.newTreeRepositories(1);
        RepositoryTree repository = gitRepRepository.findByRepositoryId(0);
        Assertions.assertEquals(2,repository.getFiles().size());
        Assertions.assertEquals(2,repository.getDirectories().size());
        Assertions.assertEquals(2,repository.getDirectories().getFirst().getFiles().size());
        Assertions.assertEquals(2,repository.getDirectories().get(1).getFiles().size());
    }

    @Test
    @DirtiesContext
    public void updateTest(){
        testHelper.newTreeRepositories(2);
        update();
        RepositoryTree repositoryTree = gitRepRepository.findByRepositoryId(1);
        Assertions.assertNull(repositoryTree.getFiles());
        Assertions.assertEquals(1,repositoryTree.getDirectories().size());
        Assertions.assertEquals(2,repositoryTree.getDirectories().getFirst().getFiles().size());
    }

    private void update() {
        RepositoryTree repositoryTree = gitRepRepository.findByRepositoryId(1);
        repositoryTree.setFiles(null);
        List<Directory> directories = new ArrayList<>();
        directories.add(repositoryTree.getDirectories().get(0));
        repositoryTree.setDirectories(directories);
        testHelper.update(repositoryTree);
    }

    @Test
    @DirtiesContext
    public void addNewFile(){
        testHelper.newTreeRepositories(1);
        serviceRepositoryTree.addNewFile(new File("/"),"/","testPath",0);
        serviceRepositoryTree.addNewFile(new File("/f"),"/1","testPath",0);
        serviceRepositoryTree.addNewFile(new File("/d/d"),"/1/3","testPath",0);
        serviceRepositoryTree.addNewFile(new File("/e/r"),"/2/3/4","testPath",0);
        serviceRepositoryTree.addNewFile(new File("/e/e"),"/5/4/1","testPath",0);
        RepositoryTree repositoryTree = gitRepRepository.findByRepositoryId(0);
        // path = "/"
        Assertions.assertEquals(3, repositoryTree.getFiles().size());
        //path = "/1"
        Assertions.assertEquals(3,repositoryTree.getDirectories().get(0).getFiles().size());
        //path = "/1/3"
        Assertions.assertEquals(1,repositoryTree.getDirectories().get(0).getDirectories().get(0).getFiles().size());
        //path = "/2/3/4"
        Assertions.assertEquals(1,repositoryTree.getDirectories().get(1).getDirectories().get(0).getDirectories().get(0).getFiles().size());
        //path = "/5/4/1"
        Assertions.assertEquals(1, repositoryTree.getDirectories().get(2).getDirectories().get(0).getDirectories().get(0).getFiles().size());
    }

    @Test
    @DirtiesContext
    public void addDirectoryTest(){
        testHelper.newTreeRepositories(1);
        serviceRepositoryTree.addNewDirectory("/7/8/9",0);
        RepositoryTree repositoryTree = gitRepRepository.findByRepositoryId(0);
        //path=/7/8/9
        Assertions.assertEquals("9",repositoryTree.getDirectories().get(2).getDirectories().get(0).getDirectories().get(0).getName());
    }
}
