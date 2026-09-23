package com.pixevent.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(String message, int status) {
        super(message);
        this.status = status;
    }

    public static ApiException badRequest(String message) {
        return new ApiException(message, 400);
    }

    public static ApiException notFound(String message) {
        return new ApiException(message, 404);
    }

    public static ApiException conflict(String message) {
        return new ApiException(message, 409);
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(message, 401);
    }
}
