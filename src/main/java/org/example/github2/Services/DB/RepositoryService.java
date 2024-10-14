package org.example.github2.Services.DB;

import org.example.github2.Entity.Repository;
import org.example.github2.Entity.User;
import org.example.github2.Repositoryes.RepositoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepositoryService {
    private final RepositoryRepository repositoryRepository;

    public RepositoryService(RepositoryRepository repositoryRepository) {
        this.repositoryRepository = repositoryRepository;
    }

    public int addRepository(Repository repository){
        repositoryRepository.save(repository);
        return repositoryRepository.findAll().getLast().getId();
    }

    public Repository findByNameAndOwner(String name , User owner){
        return repositoryRepository.findByNameAndOwner(name ,owner);
    }
}
