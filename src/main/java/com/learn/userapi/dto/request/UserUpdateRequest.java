package com.learn.userapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UserUpdateRequest {

    // No @NotBlank — name is optional on update
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    // No @NotBlank — email is optional on update, but must be valid if provided
    @Email(message = "Must be a valid email address")
    private String email;

    public UserUpdateRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
