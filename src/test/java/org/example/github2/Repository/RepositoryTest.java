package org.example.github2.Repository;

import org.example.github2.VersionControllerService.Entity.RepositoryTree;
import org.example.github2.VersionControllerService.Models.Directory;
import org.example.github2.VersionControllerService.Models.File;
import org.example.github2.VersionControllerService.Repositories.GitRepRepository;
import org.example.github2.VersionControllerService.Service.ServiceRepositoryTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class RepositoryTest {
    @Autowired
    private GitRepRepository gitRepRepository;
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
    public void saveTest(){
        testAssistant.newRepositories(1);
        RepositoryTree repository = gitRepRepository.findByRepositoryId(0);
        Assertions.assertEquals(2,repository.getFiles().size());
        Assertions.assertEquals(2,repository.getDirectories().size());
        Assertions.assertEquals(2,repository.getDirectories().getFirst().getFiles().size());
        Assertions.assertEquals(2,repository.getDirectories().get(1).getFiles().size());
    }

    @Test
    public void updateTest(){
        testAssistant.newRepositories(2);
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
        testAssistant.update(repositoryTree);
    }

    @Test
    public void addNewFile(){
        testAssistant.newRepositories(1);
        serviceRepositoryTree.addNewFile(new File("/",null),"/",0);
        serviceRepositoryTree.addNewFile(new File("/f",null),"/1",0);
        serviceRepositoryTree.addNewFile(new File("/d/d",null),"/1/3",0);
        serviceRepositoryTree.addNewFile(new File("/e/r",null),"/2/3/4",0);
        RepositoryTree repositoryTree = gitRepRepository.findByRepositoryId(0);
        // path = "/"
        Assertions.assertEquals(3, repositoryTree.getFiles().size());
        //path = "/1"
        Assertions.assertEquals(3,repositoryTree.getDirectories().get(0).getFiles().size());
        //path = "/1/3"
        Assertions.assertEquals(1,repositoryTree.getDirectories().get(0).getDirectories().get(0).getFiles().size());
        //path = "/2/3/4"
        Assertions.assertEquals(1,repositoryTree.getDirectories().get(1).getDirectories().get(0).getDirectories().get(0).getFiles().size());
    }
}
