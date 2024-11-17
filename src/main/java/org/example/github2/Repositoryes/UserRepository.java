package org.example.github2.Repositoryes;

import org.example.github2.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);
    User findById(int id);
    User findByLogin(String login);
    boolean existsByEmail(String email);
    boolean existsByLogin(String login);
}