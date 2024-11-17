package org.example.github2.VersionControllerService.Repositories;

import org.example.github2.VersionControllerService.Entity.RepositoryTree;
import org.example.github2.VersionControllerService.Models.Directory;
import org.example.github2.VersionControllerService.Models.File;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GitRepRepository extends MongoRepository<RepositoryTree,Integer> {
    RepositoryTree findByRepositoryId(int id);
}
