package com.financialtoolkit.common.api;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        List<String> messages,
        String path
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, List.of(message), path);
    }

    public static ApiError of(int status, String error, List<String> messages, String path) {
        return new ApiError(Instant.now(), status, error, messages, path);
    }
}
