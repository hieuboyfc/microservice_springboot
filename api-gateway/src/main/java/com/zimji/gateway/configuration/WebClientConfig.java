package com.zimji.gateway.configuration;

import com.zimji.gateway.client.AuthClient;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebClientConfig implements WebFluxConfigurer {

    static Logger LOGGER = LoggerFactory.getLogger(WebClientConfig.class);

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("*")); // List.of("https://example.com")
        corsConfiguration.setAllowedHeaders(List.of("*")); // List.of("Authorization", "Content-Type"))
        corsConfiguration.setAllowedMethods(List.of("*")); // List.of("GET", "POST", "PUT", "DELETE")
        corsConfiguration.setExposedHeaders(List.of("Authorization", "X-Auth-Token")); // Tiêu đề phản hồi được lộ ra
        corsConfiguration.setAllowCredentials(true); // Cho phép cookie hoặc thông tin xác thực

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsWebFilter(urlBasedCorsConfigurationSource);
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8881/auth")
                .build();
    }

    @Bean
    public AuthClient authClient(WebClient webClient) {
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();

        return httpServiceProxyFactory.createClient(AuthClient.class);
    }

    @Bean
    @Order(1)
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(5000)) // Timeout cho phản hồi
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // Timeout khi kết nối
                .secure(sslContextSpec -> {
                    try {
                        sslContextSpec.sslContext(
                                SslContextBuilder.forClient()
                                        .protocols("TLSv1.2") // Chọn phiên bản TLS
                                        .build()
                        );
                    } catch (SSLException e) {
                        throw new RuntimeException(e);
                    }
                });

        return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(config -> config.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 16MB
                        .build())
                .defaultHeader("Accept", "application/json")
                .defaultHeader("User-Agent", "GatewayService")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(retryFilter())
                .filter(rateLimitingFilter())
                .filter(metricsFilter())
                .filter(headerEnrichmentFilter());
    }

    private ExchangeFilterFunction metricsFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            LOGGER.info("---> Request: " + clientRequest.method() + " " + clientRequest.url());
            clientRequest
                    .headers()
                    .forEach((name, values) ->
                            values.forEach(value -> LOGGER.info(name + ": " + value))
                    );
            return Mono.just(clientRequest);
        }).andThen(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            LOGGER.info("---> Response Status: " + clientResponse.statusCode());
            clientResponse
                    .headers()
                    .asHttpHeaders()
                    .forEach((name, values) ->
                            values.forEach(value -> LOGGER.info(name + ": " + value))
                    );
            return Mono.just(clientResponse);
        }));
    }

    private ExchangeFilterFunction retryFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse ->
                Mono.defer(() -> {
                    if (clientResponse.statusCode().is4xxClientError()
                            || clientResponse.statusCode().is5xxServerError()) {
                        return Mono.just(clientResponse)
                                .retryWhen(retrySpec())
                                .flatMap(Mono::just); // Trả về phản hồi gốc sau khi retry
                    } else {
                        return Mono.just(clientResponse); // Nếu không có lỗi, trả về phản hồi gốc
                    }
                })
        );
    }

    private Retry retrySpec() {
        return Retry
                .backoff(3, Duration.ofSeconds(1)) // Retry 3 lần với khoảng cách thời gian là 1 giây
                .maxBackoff(Duration.ofSeconds(10)) // Thời gian chờ tối đa là 10 giây
                .filter(throwable -> throwable instanceof IOException || throwable instanceof TimeoutException); // Retry chỉ cho các lỗi nhất định
    }

    private ExchangeFilterFunction rateLimitingFilter() {
        // Tạo cấu hình cho bucket với giới hạn 100 yêu cầu mỗi phút
        Bandwidth bandwidth = Bandwidth.simple(100, Duration.ofMinutes(1));

        Bucket bucket = Bucket.builder()
                .addLimit(bandwidth)
                .build();

        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            if (probe.isConsumed()) {
                return Mono.just(clientRequest);
            } else {
                return Mono.error(new RuntimeException("Rate limit exceeded. Retry in " + probe.getNanosToWaitForRefill() + " nanoseconds."));
            }
        });
    }

    private ExchangeFilterFunction headerEnrichmentFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            ClientRequest newRequest = ClientRequest.from(clientRequest)
                    .headers(headers -> headers.add("X-Custom-Header", "value"))
                    .build();
            return Mono.just(newRequest);
        });
    }

}
