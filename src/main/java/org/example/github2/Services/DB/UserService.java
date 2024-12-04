package org.example.github2.Services.DB;

import org.example.github2.Converters.UserConverter;
import org.example.github2.DTOs.UserDTO;
import org.example.github2.Entity.User;
import org.example.github2.Repositoryes.UserRepository;
import org.example.github2.Security.SHA256;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserConverter userConverter = new UserConverter();
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void save(User newUser){
        userRepository.save(newUser);
    }

    public void update(User user){
        userRepository.save(user);
    }

    public UserDTO findUserDtoByEmail(String email){
        User user = findUserByEmail(email);
        if (user!=null) return userConverter.userToUserDTO(user);
        return null;
    }

    public UserDTO findUserDtoByLogin(String login){
        User user = findUserByLogin(login);
        if (user!=null) return userConverter.userToUserDTO(user);
        return null;
    }

    public User findUserByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public User findUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public User findUserById(int id){
        return userRepository.findById(id);
    }

    public boolean isCorrectInputValidEmailCode(int userId,String inputCode){
        User user = findUserById(userId);
        String correctCode = user.getEmailCode();
        return inputCode!=null && correctCode.equals(SHA256.hash(inputCode));
    }

    public boolean isAllowedResendEmail(String email) {
        User user = findUserByEmail(email);
        return user.getTimeLastSendCode().plusMinutes(2).isBefore(LocalDateTime.now());
    }

    public boolean isExistsUser(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isBusyLogin(String login){
        return userRepository.existsByLogin(login);
    }

    public boolean isCorrectPassword(String email, String inputPassword) {
        User user = findUserByEmail(email);
        return user.getPassword().equals(SHA256.hash(inputPassword));
    }
}
