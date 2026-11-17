package com.iot.admin.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class ControllerLogAspect {

    private static final Logger log = LoggerFactory.getLogger(ControllerLogAspect.class);
    private static final int MAX_LOG_LENGTH = 2048;
    private static final String MASK = "***";

    private final ObjectMapper objectMapper;

    public ControllerLogAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Around("@within(org.springframework.web.bind.annotation.RestController) && within(com.iot.admin..*)")
    public Object logControllerCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        String request = requestLabel();
        String traceId = MDC.get(TraceIdFilter.MDC_KEY);
        log.info("API traceId={} {} {} input={}", traceId, request, method, format(joinPoint.getArgs()));
        long start = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            Object body = result instanceof ResponseEntity<?> entity ? entity.getBody() : result;
            log.info("API traceId={} {} {} output={} elapsedMs={}", traceId, request, method, format(body),
                    (System.nanoTime() - start) / 1_000_000);
            return result;
        } catch (Throwable exception) {
            log.warn("API traceId={} {} {} failed elapsedMs={} errorType={}", traceId, request, method,
                    (System.nanoTime() - start) / 1_000_000, exception.getClass().getSimpleName());
            throw exception;
        }
    }

    private String requestLabel() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            return request.getMethod() + " " + request.getRequestURI();
        }
        return "unknown";
    }

    String format(Object value) {
        try {
            JsonNode tree = objectMapper.valueToTree(value);
            mask(tree);
            String text = objectMapper.writeValueAsString(tree);
            return text.length() > MAX_LOG_LENGTH ? text.substring(0, MAX_LOG_LENGTH) + "...[truncated]" : text;
        } catch (Exception exception) {
            return "[unavailable]";
        }
    }

    private void mask(JsonNode node) {
        if (node instanceof ObjectNode object) {
            object.properties().forEach(entry -> {
                if (isSensitive(entry.getKey())) {
                    object.put(entry.getKey(), MASK);
                } else {
                    mask(entry.getValue());
                }
            });
        } else if (node instanceof ArrayNode array) {
            array.forEach(this::mask);
        }
    }

    private boolean isSensitive(String field) {
        String name = field.toLowerCase(Locale.ROOT);
        return name.contains("password") || name.contains("secret") || name.contains("token")
                || name.contains("authorization") || name.contains("cookie") || name.contains("credential")
                || name.contains("apikey") || name.contains("privatekey");
    }
}
