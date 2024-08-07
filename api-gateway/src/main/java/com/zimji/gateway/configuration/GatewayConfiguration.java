package com.zimji.gateway.configuration;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GatewayConfiguration {

    AppProperties appProperties;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        AppProperties.AuthServiceInfo authServiceInfo = appProperties.getAuthServiceInfo();
        return builder.routes()
                .route(authServiceInfo.getName(), r -> r
                        .path(authServiceInfo.getPath())
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("User-Agent", "CustomUserAgent")
                                .addResponseHeader("Cache-Control", "no-cache")
                                .retry(3)
                        )
                        .uri(authServiceInfo.getUrl())
                )
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return RouterFunctions.route()
                .GET("/fallback", this::fallback)
                .build();
    }

    public Mono<ServerResponse> fallback(ServerRequest request) {
        return ServerResponse.ok().bodyValue("Service is temporarily unavailable. Please try again later.");
    }

}
