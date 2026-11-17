package com.iot.admin.response;

public record R<T>(int code, String traceId, String msg, T data) {

    public static <T> R<T> of(int code, T data) {
        return new R<>(code, "", "", data);
    }

    public static R<Void> error(int code, String msg) {
        return new R<>(code, "", msg, null);
    }
}
