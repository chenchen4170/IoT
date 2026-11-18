package com.iot.admin.common;

import org.slf4j.MDC;

public record R<T>(int code, String traceId, String msg, T data) {

    public static <T> R<T> of(int code, T data) {
        return new R<>(code, currentTraceId(), "", data);
    }

    public static R<Void> error(int code, String msg) {
        return new R<>(code, currentTraceId(), msg, null);
    }

    private static String currentTraceId() {
        String traceId = MDC.get(TraceIdFilter.MDC_KEY);
        return traceId == null ? "" : traceId;
    }
}
