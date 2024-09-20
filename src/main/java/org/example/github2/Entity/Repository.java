package org.example.github2.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "repositories")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Repository {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "is_private")
    private Boolean isPrivate;
    @ManyToOne()
    @JoinColumn(name = "owner")
    private User owner;

    public Repository(String name, Boolean isPrivate, User owner) {
        this.name = name;
        this.isPrivate = isPrivate;
        this.owner = owner;
    }
}
