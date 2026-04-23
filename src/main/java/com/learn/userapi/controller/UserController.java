package com.learn.userapi.controller;

import com.learn.userapi.model.User;
import com.learn.userapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController  // Bean + serialize return values to JSON
@RequestMapping("/api/users")  // base URL prefix for all methods in this class
public class UserController {

    private final UserService userService;

    // constructor injection — Spring injects UserService automatically
    public UserController(UserService userService) {
        this.userService = userService;
        System.out.println(">>> UserController created. UserService injected.");
    }

    // GET /api/users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)// found -> 200 OK
                .orElse(ResponseEntity.notFound().build()); // not found -> 404
    }

    // POST /api/users
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserRequest request) {
        try {
            User created =  userService.createUser(request.getName(), request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(created); // 201 Created
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // 400 Bad Request
        }
    }

    // PUT /api/users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        return userService.updateUser(id, request.getName(), request.getEmail())
                .map(ResponseEntity::ok) // updated -> 200 OK
                .orElse(ResponseEntity.notFound().build()); // not found -> 404
    }

    // DELETE /api/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<User> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        return deleted
                ? ResponseEntity.ok().build()  // 204 No Content
                : ResponseEntity.notFound().build();  // 404 Not found
    }

}
