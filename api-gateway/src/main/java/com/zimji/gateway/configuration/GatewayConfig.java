package com.zimji.gateway.configuration;

import com.zimji.gateway.payload.response.BaseResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFlux
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GatewayConfig {

    static Logger LOGGER = LoggerFactory.getLogger(Resilience4JConfig.class);

    AppProperties appProperties;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        AppProperties.AuthServiceInfo authServiceInfo = appProperties.getAuthServiceInfo();
        return builder.routes()
                .route(authServiceInfo.getName(), route -> route
                        .path(authServiceInfo.getPath())
                        .filters(filter -> filter
                                .stripPrefix(1)
                                .addRequestHeader("User-Agent", "CustomUserAgent")
                                .addResponseHeader("Cache-Control", "no-cache")
                                .retry(3)
                                .filter((exchange, chain) -> {
                                    // Thực hiện gọi API báo lỗi
                                    return callErrorApi(exchange).then(chain.filter(exchange));
                                })
                        )
                        .uri(authServiceInfo.getUrl())
                )
                .build();
    }

    private Mono<Void> callErrorApi(ServerWebExchange exchange) {
        // Giả sử bạn có một RestTemplate hoặc WebClient để gọi API báo lỗi
        // Ví dụ sử dụng WebClient:
        WebClient webClient = WebClient.create();
        return webClient.get()
                .uri("http://localhost:1997/fallback")
                .retrieve()
                .bodyToMono(BaseResponse.class)
                .doOnSuccess(response -> {
                    LOGGER.info("---> Success: {}", response.getResult());
                })
                .doOnError(throwable -> {
                    LOGGER.error("---> Error: {}", throwable.getMessage());
                })
                .then(Mono.empty());
    }

}
