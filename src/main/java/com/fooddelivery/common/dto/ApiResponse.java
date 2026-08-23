package com.fooddelivery.common.dto;

import java.time.LocalDateTime;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder().success(false).message(message).build();
    }

private static <T> LocalDateTime $default$timestamp() {
        return LocalDateTime.now();
    }


public static class ApiResponseBuilder<T> {
private boolean success;
private String message;
private T data;
private boolean timestamp$set;
private LocalDateTime timestamp$value;

ApiResponseBuilder() {
        }

        /**
         * @return {@code this}.
         */
public ApiResponse.ApiResponseBuilder<T> success(final boolean success) {
            this.success = success;
            return this;
        }

        /**
         * @return {@code this}.
         */
public ApiResponse.ApiResponseBuilder<T> message(final String message) {
            this.message = message;
            return this;
        }

        /**
         * @return {@code this}.
         */
public ApiResponse.ApiResponseBuilder<T> data(final T data) {
            this.data = data;
            return this;
        }

        /**
         * @return {@code this}.
         */
public ApiResponse.ApiResponseBuilder<T> timestamp(final LocalDateTime timestamp) {
            this.timestamp$value = timestamp;
            timestamp$set = true;
            return this;
        }

public ApiResponse<T> build() {
            LocalDateTime timestamp$value = this.timestamp$value;
            if (!this.timestamp$set) timestamp$value = ApiResponse.<T>$default$timestamp();
            return new ApiResponse<T>(this.success, this.message, this.data, timestamp$value);
        }

        @java.lang.Override
public java.lang.String toString() {
            return "ApiResponse.ApiResponseBuilder(success=" + this.success + ", message=" + this.message + ", data=" + this.data + ", timestamp$value=" + this.timestamp$value + ")";
        }
    }

public static <T> ApiResponse.ApiResponseBuilder<T> builder() {
        return new ApiResponse.ApiResponseBuilder<T>();
    }

public boolean isSuccess() {
        return this.success;
    }

public String getMessage() {
        return this.message;
    }

public T getData() {
        return this.data;
    }

public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

public void setSuccess(final boolean success) {
        this.success = success;
    }

public void setMessage(final String message) {
        this.message = message;
    }

public void setData(final T data) {
        this.data = data;
    }

public void setTimestamp(final LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @java.lang.Override
public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof ApiResponse)) return false;
        final ApiResponse<?> other = (ApiResponse<?>) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (this.isSuccess() != other.isSuccess()) return false;
        final java.lang.Object this$message = this.getMessage();
        final java.lang.Object other$message = other.getMessage();
        if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
        final java.lang.Object this$data = this.getData();
        final java.lang.Object other$data = other.getData();
        if (this$data == null ? other$data != null : !this$data.equals(other$data)) return false;
        final java.lang.Object this$timestamp = this.getTimestamp();
        final java.lang.Object other$timestamp = other.getTimestamp();
        if (this$timestamp == null ? other$timestamp != null : !this$timestamp.equals(other$timestamp)) return false;
        return true;
    }

protected boolean canEqual(final java.lang.Object other) {
        return other instanceof ApiResponse;
    }

    @java.lang.Override
public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isSuccess() ? 79 : 97);
        final java.lang.Object $message = this.getMessage();
        result = result * PRIME + ($message == null ? 43 : $message.hashCode());
        final java.lang.Object $data = this.getData();
        result = result * PRIME + ($data == null ? 43 : $data.hashCode());
        final java.lang.Object $timestamp = this.getTimestamp();
        result = result * PRIME + ($timestamp == null ? 43 : $timestamp.hashCode());
        return result;
    }

    @java.lang.Override
public java.lang.String toString() {
        return "ApiResponse(success=" + this.isSuccess() + ", message=" + this.getMessage() + ", data=" + this.getData() + ", timestamp=" + this.getTimestamp() + ")";
    }

public ApiResponse() {
        this.timestamp = ApiResponse.$default$timestamp();
    }

public ApiResponse(final boolean success, final String message, final T data, final LocalDateTime timestamp) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }
}
