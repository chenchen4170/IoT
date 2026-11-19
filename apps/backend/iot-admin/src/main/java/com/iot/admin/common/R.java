package com.iot.admin.common;

import org.slf4j.MDC;

public record R<T>(String code, String traceId, String msg, T data) {

    public static <T> R<T> of(int status, T data) {
        return new R<>(Integer.toString(status), currentTraceId(), "", data);
    }

    public static R<Void> error(String code, String msg) {
        return new R<>(code, currentTraceId(), msg, null);
    }

    private static String currentTraceId() {
        String traceId = MDC.get(TraceIdFilter.MDC_KEY);
        return traceId == null ? "" : traceId;
    }
}
