package com.zimji.gateway.configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnErrorEvent;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnSuccessEvent;
import io.github.resilience4j.core.EventConsumer;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Resilience4JConfig {

    static Logger LOGGER = LoggerFactory.getLogger(Resilience4JConfig.class);

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> globalCustomConfiguration() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(timeLimiterConfig())
                .circuitBreakerConfig(circuitBreakerConfig())
                .build());
    }

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> specificCustomConfiguration() {
        return factory -> {
            factory.configure(builder -> builder
                            .timeLimiterConfig(timeLimiterConfig())
                            .circuitBreakerConfig(circuitBreakerConfig()),
                    "circuitBreaker1", "circuitBreaker2", "circuitBreaker3");

            factory.addCircuitBreakerCustomizer(circuitBreaker -> {
                circuitBreaker.getEventPublisher()
                        .onError(normalFluxErrorConsumer())
                        .onSuccess(normalFluxSuccessConsumer());
            }, "circuitBreaker1");
        };
    }

    @Bean
    public TimeLimiterConfig timeLimiterConfig() {
        return TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .build();
    }

    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .slidingWindowSize(2)
                .failureRateThreshold(50)
                .permittedNumberOfCallsInHalfOpenState(5)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .slowCallDurationThreshold(Duration.ofMillis(200))
                .slowCallRateThreshold(50)
                .build();
    }

    @Bean
    public EventConsumer<CircuitBreakerOnErrorEvent> normalFluxErrorConsumer() {
        return event -> {
            LOGGER.info("CircuitBreaker Error Event: " + event);

            // Thực hiện các hành động tùy chỉnh khác nếu cần
            // Ví dụ: gửi thông báo, ghi vào cơ sở dữ liệu, v.v.
        };
    }

    @Bean
    public EventConsumer<CircuitBreakerOnSuccessEvent> normalFluxSuccessConsumer() {
        return event -> {
            LOGGER.info("CircuitBreaker Success Event: " + event);

            // Thực hiện các hành động tùy chỉnh khác nếu cần
            // Ví dụ: gửi thông báo, ghi vào cơ sở dữ liệu, v.v.
        };
    }

}