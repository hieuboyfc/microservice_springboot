package com.zimji.gateway.configuration;

import com.zimji.gateway.exception.WebClientExceptionHandler;
import com.zimji.gateway.payload.response.BaseResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFlux
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GatewayConfig {

    static Logger LOGGER = LoggerFactory.getLogger(GatewayConfig.class);

    final AppProperties appProperties;
    final WebClient webClient;

    public GatewayConfig(AppProperties appProperties, WebClient.Builder webClient) {
        this.appProperties = appProperties;
        this.webClient = webClient.build();
    }

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
                                .filter((exchange, chain) -> {
                                    // Thực hiện gọi API báo lỗi
                                    return callErrorApi(exchange).then(chain.filter(exchange));
                                })
                                .retry(3)
                        )
                        .uri(authServiceInfo.getUrl())
                )
                .build();
    }

    private Mono<Void> callErrorApi(ServerWebExchange exchange) {
        // Giả sử bạn có một RestTemplate hoặc WebClient để gọi API báo lỗi
        return webClient.get()
                .uri("http://localhost:1997/fallback")
                .retrieve()
                .bodyToMono(BaseResponse.class)
                .log()
                .doOnSuccess(response -> {
                    LOGGER.info("---> Success: {}", response.getResult());
                })
                .doOnError(throwable -> {
                    LOGGER.error("---> Error: {}", throwable.getMessage());
                })
                .onErrorResume(WebClientResponseException.class, exception ->
                        WebClientExceptionHandler.handleException(exception)
                                .flatMap(response -> Mono.error(new ResponseStatusException(
                                        HttpStatus.valueOf(Integer.parseInt(response.getCode())),
                                        response.getMessage()
                                )))
                )
                .then(Mono.empty());
    }

}
