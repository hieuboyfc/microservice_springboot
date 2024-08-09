package com.zimji.gateway.configuration;

import com.zimji.gateway.client.AuthClient;
import com.zimji.gateway.payload.response.BaseResponse;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.WebFluxConfigurer;
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
public class WebClientConfig implements WebFluxConfigurer {

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
                .defaultHeader("User-Agent", "Gateway-Service")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(logRequest())
                .filter(logResponse())
                .filter(retryFilter()); // Đưa filter retry vào cuối chuỗi
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            System.out.println("---> Request: " + clientRequest.method() + " " + clientRequest.url());
            clientRequest.headers().forEach((name, values) ->
                    values.forEach(value -> System.out.println(name + ": " + value)));
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            System.out.println("---> Response Status: " + clientResponse.statusCode());
            clientResponse.headers().asHttpHeaders().forEach((name, values) ->
                    values.forEach(value -> System.out.println(name + ": " + value)));
            return Mono.just(clientResponse);
        });
    }

    private ExchangeFilterFunction retryFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse ->
                clientResponse.bodyToMono(BaseResponse.class)
                        .retryWhen(retrySpec()) // Áp dụng retry logic
                        .then(Mono.just(clientResponse)) // Trả về phản hồi gốc sau khi retry
        );
    }

    private Retry retrySpec() {
        return Retry
                .backoff(3, Duration.ofSeconds(1)) // Retry 3 lần với khoảng cách thời gian là 1 giây
                .maxBackoff(Duration.ofSeconds(10)) // Thời gian chờ tối đa là 10 giây
                .filter(throwable -> throwable instanceof IOException || throwable instanceof TimeoutException); // Retry chỉ cho các lỗi nhất định
    }

}
