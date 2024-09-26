package org.example.github2.VersionControllerService.Service;

import org.example.github2.VersionControllerService.Entity.RepositoryTree;
import org.example.github2.VersionControllerService.Repositories.GitRepRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class ServiceRepositoryTree {
    private final GitRepRepository gitRepRepository;
    private final MongoTemplate mongoTemplate;

    public ServiceRepositoryTree(GitRepRepository gitRepRepository, MongoTemplate mongoTemplate) {
        this.gitRepRepository = gitRepRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public void save(RepositoryTree newRepositoryTree){
        gitRepRepository.save(newRepositoryTree);
    }

    public void update(RepositoryTree updateRepositoryTree){
        Update update = new Update();
        update.set("files", updateRepositoryTree.getFiles());
        update.set("directories", updateRepositoryTree.getDirectories());
        mongoTemplate.findAndModify(
                query(where("repositoryId").is(updateRepositoryTree.getRepositoryId())),
                update,
                RepositoryTree.class
        );
    }
}
