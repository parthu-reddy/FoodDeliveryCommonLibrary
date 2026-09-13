package com.fooddelivery.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.LocalDateTime;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class ApiResponse<T> {
    @Schema(requiredMode = RequiredMode.REQUIRED)
    private boolean success;
    @Schema(requiredMode = RequiredMode.REQUIRED)
    private String message;
    private String errorCode;
    private T data;
    
    @lombok.Builder.Default
    @Schema(requiredMode = RequiredMode.REQUIRED)
    private LocalDateTime timestamp = LocalDateTime.now();


    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder().success(false).message(message).build();
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder().success(false).message(message).errorCode(errorCode).build();
    }
}
