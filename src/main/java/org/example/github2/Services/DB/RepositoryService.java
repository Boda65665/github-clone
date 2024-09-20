package org.example.github2.Services.DB;

import org.example.github2.Entity.Repository;
import org.example.github2.Entity.User;
import org.example.github2.Repositoryes.RepositoryRepository;
import org.springframework.stereotype.Service;

@Service
public class RepositoryService {
    private final RepositoryRepository repositoryRepository;

    public RepositoryService(RepositoryRepository repositoryRepository) {
        this.repositoryRepository = repositoryRepository;
    }

    public void addRepository(Repository repository){
        repositoryRepository.save(repository);
    }

    public Repository findByNameAndOwner(String name , User owner){
        return repositoryRepository.findByNameAndOwner(name ,owner);
    }
}
