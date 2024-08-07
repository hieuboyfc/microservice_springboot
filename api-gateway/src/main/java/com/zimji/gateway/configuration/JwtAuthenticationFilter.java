package com.zimji.gateway.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimji.gateway.payload.response.BaseResponse;
import com.zimji.gateway.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    GatewayProperties gatewayProperties;
    AuthService authService;
    ObjectMapper objectMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        LOGGER.info("---> Enter Authentication filter...");

        if (isPublicEndpoint(exchange.getRequest())) {
            return chain.filter(exchange);
        }

        // Lấy token từ header của yêu cầu
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (ObjectUtils.isEmpty(authHeader)) {
            LOGGER.error("---> Unauthorized request: Missing or invalid Authorization header");
            return unauthenticated(exchange.getResponse());
        }

        String token = authHeader;
        if (token.startsWith("Bearer ")) {
            // Lấy token từ header
            token = authHeader.substring(7); // "Bearer " là 7 ký tự
        }
        LOGGER.info("---> Token: {}", token);

        return authService.introspect(token).flatMap(response -> {
            if (response.getResult().isValid()) {
                // Nếu token hợp lệ, tiếp tục xử lý yêu cầu
                return chain.filter(exchange);
            } else {
                // Nếu token không hợp lệ, trả về lỗi 401 Unauthorized
                LOGGER.error("---> Unauthorized request: Invalid token");
                return unauthenticated(exchange.getResponse());
            }
        }).onErrorResume(throwable -> unauthenticated(exchange.getResponse()));
    }

    @Override
    public int getOrder() {
        return -1;
    }

    Mono<Void> unauthenticated(ServerHttpResponse response) {
        BaseResponse<?> baseResponse = BaseResponse.builder()
                .code("401")
                .message("Unauthenticated")
                .timestamp(new Date())
                .build();

        String body;
        try {
            body = objectMapper.writeValueAsString(baseResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8)))
        );
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        String[] publicEndpoints = gatewayProperties.getPublicEndPoints().split(",");
        return Arrays
                .stream(publicEndpoints)
                .anyMatch(item -> request.getURI().getPath().matches(gatewayProperties.getApiPrefix() + item));
    }

}
