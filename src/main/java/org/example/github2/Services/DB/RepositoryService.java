package org.example.github2.Services.DB;

import org.example.github2.Entity.Repository;
import org.example.github2.Entity.User;
import org.example.github2.Repositoryes.RepositoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepositoryService {
    private final RepositoryRepository repositoryRepository;
    private final UserService userService;

    public RepositoryService(RepositoryRepository repositoryRepository, UserService userService) {
        this.repositoryRepository = repositoryRepository;
        this.userService = userService;
    }

    public int addRepository(Repository repository){
        repositoryRepository.save(repository);
        return repositoryRepository.findAll().getLast().getId();
    }

    public Repository findByNameAndOwner(String name , User owner){
        return repositoryRepository.findByNameAndOwner(name ,owner);
    }

    public Repository findByNameAndOwner(String name , String nameOwner){
        return repositoryRepository.findByNameAndOwner(name ,userService.findUserByLogin(nameOwner));
    }

    public Repository findById(int id){
        return repositoryRepository.findById(id);
    }
}
