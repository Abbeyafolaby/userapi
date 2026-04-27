package com.learn.userapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final Long resourceId;

    public ResourceNotFoundException(String resourceName, Long resourceId) {
        super(resourceName + " not found with id " + resourceId);
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }


    public String getResourceName() {
        return resourceName;
    }

    public Long getResourceId() {
        return resourceId;
    }
}
