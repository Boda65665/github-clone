package org.example.github2.Repositoryes;

import org.example.github2.Entity.Repository;
import org.example.github2.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositoryRepository extends JpaRepository<Repository, Integer> {
    Repository findByNameAndOwner(String name , User owner);
    Repository findById(int id);
}
