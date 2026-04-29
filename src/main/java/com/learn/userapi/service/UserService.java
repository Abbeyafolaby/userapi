package com.learn.userapi.service;

import com.learn.userapi.dto.request.UserCreateRequest;
import com.learn.userapi.dto.request.UserUpdateRequest;
import com.learn.userapi.dto.response.UserResponse;
import com.learn.userapi.exception.DuplicateResourceException;
import com.learn.userapi.exception.ResourceNotFoundException;
import com.learn.userapi.model.User;
import com.learn.userapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        log.info("UserService initialized");
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(UserResponse::fromUser)
                .toList();
        log.info("Retrieved {} users", users.size());
        return users;
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user with id: {}", id);
        return userRepository.findById(id)
                .map(user -> {
                    log.info("User found with id: {}", id);
                    return UserResponse.fromUser(user);
                })
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new ResourceNotFoundException("User", id);
                });
    }

    public UserResponse createUser(UserCreateRequest request) {
        log.debug("Creating user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }

        User user = new User(request.getName(), request.getEmail());
        UserResponse created = UserResponse.fromUser(userRepository.save(user));
        log.info("User created successfully with id: {}", created.getId());
        return created;
    }

    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.debug("Updating user with id: {}", id);
        User existing = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed — user not found with id: {}", id);
                    return new ResourceNotFoundException("User", id);
                });

        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getEmail() != null) {
            existing.setEmail(request.getEmail());
        }

        UserResponse updated = UserResponse.fromUser(userRepository.save(existing));
        log.info("User updated successfully with id: {}", id);
        return updated;
    }

    public void deleteUser(Long id) {
        log.debug("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("Delete failed — user not found with id: {}", id);
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
        log.info("User deleted successfully with id: {}", id);
    }
}