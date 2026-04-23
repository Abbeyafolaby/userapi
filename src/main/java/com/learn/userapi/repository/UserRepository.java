package com.learn.userapi.repository;

import com.learn.userapi.model.User;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepository {
    private final Map<Long, User> store = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @PostConstruct
    public void init() {
        // runs AFTER Spring has created and injected everything into this Bean
        User seed = new User(idCounter.getAndIncrement(), "Alice", "alice@example.com");
        store.put(seed.getId(), seed);
        System.out.println(">>> UserRepository initialized. Seed data loaded: " + seed);
    }

    @PreDestroy
    public void cleanup() {
        // runs BEFORE Spring destroys this Bean (app shutdown)
        System.out.println(">>> UserRepository shutting down. Clearing store");
        store.clear();
    }

    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idCounter.getAndIncrement());
        }
        store.put(user.getId(), user);
        return user;
    }

    public boolean deleteById(Long id) {
        return store.remove(id) != null;
    }

    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

}
