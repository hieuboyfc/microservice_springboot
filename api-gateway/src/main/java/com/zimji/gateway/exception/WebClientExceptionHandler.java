package com.zimji.gateway.exception;

import com.zimji.gateway.payload.response.BaseResponse;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Date;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class WebClientExceptionHandler {

    static final Logger LOGGER = LoggerFactory.getLogger(WebClientExceptionHandler.class);

    public static Mono<BaseResponse<?>> handleException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException exception) {
            LOGGER.error("---> WebClient Error: Status code: {}, Reason: {}, Body: {}",
                    exception.getStatusCode(),
                    exception.getStatusText(),
                    exception.getResponseBodyAsString());

            BaseResponse<?> errorResponse = BaseResponse.builder()
                    .code(String.valueOf(exception.getStatusCode().value()))
                    .title("Error")
                    .message(exception.getStatusText())
                    .description(exception.getResponseBodyAsString())
                    .timestamp(new Date())
                    .build();

            return Mono.error(new CustomException(errorResponse));
        } else {
            LOGGER.error("---> Unexpected Error: {}", throwable.getMessage(), throwable);

            BaseResponse<?> errorResponse = BaseResponse.builder()
                    .code("500")
                    .title("Internal Server Error")
                    .message("An unexpected error occurred")
                    .timestamp(new Date())
                    .build();

            return Mono.error(new CustomException(errorResponse));
        }
    }

    @Getter
    public static class CustomException extends RuntimeException {
        private final BaseResponse<?> baseResponse;

        public CustomException(BaseResponse<?> baseResponse) {
            super(baseResponse.getMessage());
            this.baseResponse = baseResponse;
        }
    }

}
