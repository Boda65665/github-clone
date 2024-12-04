package org.example.github2.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.github2.Model.Role;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "password")
    private String password;
    @Column(name = "email")
    private String email;
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column(name = "code_email")
    private String emailCode;
    @Column(name = "time_send_email_code")
    private LocalDateTime timeLastSendCode;
    @OneToMany
    @JoinColumn(name = "owner")
    private List<Repository> repositories;
    @Column(name = "login")
    private String login;

    public User(String email, String login, String password, Role role, String emailCode, LocalDateTime timeLastSendCode) {
        this.email = email;
        this.login = login;
        this.password = password;
        this.role = role;
        this.emailCode = emailCode;
        this.timeLastSendCode = timeLastSendCode;
    }
}
