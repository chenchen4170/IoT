package com.iot.admin.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "com.iot.admin")
public class CommonResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    public CommonResponseAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        boolean stringResponse = StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType);
        if (body instanceof R<?> || (!stringResponse
                && !MediaType.APPLICATION_JSON.isCompatibleWith(selectedContentType))) {
            return body;
        }

        int status = ((ServletServerHttpResponse) response).getServletResponse().getStatus();
        R<?> result = R.of(status, body);
        if (stringResponse) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            try {
                return objectMapper.writeValueAsString(result);
            } catch (JsonProcessingException exception) {
                throw new IllegalStateException("Failed to serialize response", exception);
            }
        }
        return result;
    }
}
