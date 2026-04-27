package com.learn.userapi.dto.response;

import com.learn.userapi.model.User;

public class UserResponse {

    private Long id;
    private String name;
    private String email;

    // private constructor — built only via fromUser()
    public UserResponse(Long id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    // static factory — converts internal User model to response DTO
    // this lives here so the mapping logic is co-located with the DTO itself
    public static UserResponse fromUser(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
