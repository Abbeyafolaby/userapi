package com.learn.userapi.config;

import com.learn.userapi.model.User;
import com.learn.userapi.repository.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@Profile("dev") // this entire Bean only exists in dev - Spring ignores it in prod
public class DataSeeder {

    private final UserRepository userRepository;
    private final AppInfo appInfo;

    public DataSeeder(UserRepository userRepository, AppInfo appInfo) {
        this.userRepository = userRepository;
        this.appInfo = appInfo;
    }

    @PostConstruct
    public void seed() {
        if(!appInfo.isSeedDataEnabled()) return;

        userRepository.save(new User(null, "Bob", "bob@example.com"));
        userRepository.save(new User(null, "Charlie", "charlie@example.com"));

        System.out.println(">>> [DEV] DataSeeder loaded extra users. Total: "
                + userRepository.findAll().size());
    }

}
