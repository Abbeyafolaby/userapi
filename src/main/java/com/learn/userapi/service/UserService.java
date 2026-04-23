package com.learn.userapi.service;

import com.learn.userapi.model.User;
import com.learn.userapi.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    // Constructor injection — Spring sees this and automatically injects UserRepository
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        System.out.println(">>> UserService created. UserRepository injected: " + userRepository);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(String name, String email) {
        // business logic lives here — e.g. validation rules
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        User user = new User(null, name, email);
        return userRepository.save(user);
    }

    public Optional<User> updateUser(Long id, String name, String email) {
        return userRepository.findById(id).map(existing -> {
            existing.setName(name);
            existing.setEmail(email);
            return userRepository.save(existing);
        });
    }

    public boolean deleteUser(Long id) {
        return userRepository.deleteById(id);
    }
}
