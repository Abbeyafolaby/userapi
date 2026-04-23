package com.learn.userapi.config;

import com.learn.userapi.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration // Spring: scan this class for @ Bean methods
public class AppConfig {

    @Bean
    @Profile("dev")
    public AppInfo devAppInfo() {
        System.out.println(">>> [DEV] AppInfo Bean created");
        return new AppInfo("development", "1.0.0-dev", true);
    }

    @Bean
    @Profile("prod")
    public AppInfo prodAppInfo() {
        System.out.println(">>> [PROD] AppInfo Bean created");
        return new AppInfo("production", "1.0.0", false);
    }



}
