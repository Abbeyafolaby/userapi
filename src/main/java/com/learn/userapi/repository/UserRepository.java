package com.learn.userapi.repository;

import com.learn.userapi.model.User;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

    private final Map<Long, User> store = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @PostConstruct
    public void init() {
        // runs AFTER Spring has created and injected everything into this Bean
        User seed = new User(idCounter.getAndIncrement(), "Alice", "alice@example.com");
        store.put(seed.getId(), seed);
        log.info("UserRepository initialized with seed user: id={}", seed.getId());
    }

    @PreDestroy
    public void cleanup() {
        // runs BEFORE Spring destroys this Bean (app shutdown)
        log.info("UserRepository shutting down. clearing {} records", store.size());
        store.clear();
    }

    public List<User> findAll() {
        log.debug("findAll called, Current store size: {}", store.size());
        return new ArrayList<>(store.values());
    }

    public Optional<User> findById(Long id) {
        log.debug("findById called with id {}", id);
        return Optional.ofNullable(store.get(id));
    }

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idCounter.getAndIncrement());
            log.debug("Assigning with new id {} to user", user.getId());
        }
        store.put(user.getId(), user);
        log.debug("Save user with id {}", user.getId());
        return user;
    }

    public boolean deleteById(Long id) {
        boolean removed = store.remove(id) != null;
        log.debug("deleteById id: {} removed {}", id, removed);
        return removed;
    }

    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

}
