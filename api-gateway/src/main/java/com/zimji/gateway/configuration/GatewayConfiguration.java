package com.zimji.gateway.configuration;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

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
                                .addRequestHeader(HttpHeaders.USER_AGENT, "CustomUserAgent")
                                .addResponseHeader(HttpHeaders.CACHE_CONTROL, "no-cache")
                                .retry(3)
                        )
                        .uri(authServiceInfo.getUrl())
                )
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return RouterFunctions.route()
                .GET("/fallback", request -> ServerResponse
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .bodyValue("Fallback response")
                )
                .build();
    }

}
