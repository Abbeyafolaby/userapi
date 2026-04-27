package com.learn.userapi.dto.response;

import java.time.LocalDateTime;

public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    // private constructor — force use of static factory methods below
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // static factory methods — clean, readable call sites
    public static<T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true,message, data);
    }

    public static<T> ApiResponse<T> error(String message,  T data) {
        return new ApiResponse<>(false, message, data);
    }

    public static<T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
