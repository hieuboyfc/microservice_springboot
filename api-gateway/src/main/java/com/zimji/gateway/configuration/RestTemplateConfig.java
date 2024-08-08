/*
package com.zimji.gateway.configuration;

import jakarta.annotation.Nullable;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;

@Configuration
public class RestTemplateConfig {

    static Logger LOGGER = LoggerFactory.getLogger(RestTemplateConfig.class);

    public static class LoggingInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request,
                                            @Nullable byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {
            // Log Request
            LOGGER.info("---> Request: " + request.getMethod() + " " + request.getURI());

            assert body != null;
            ClientHttpResponse response = execution.execute(request, body);

            // Log Response
            LOGGER.info("---> Response: " + response.getStatusCode() + " " + response.getStatusText());

            return response;
        }
    }

    public static class CustomErrorHandler implements ResponseErrorHandler {

        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return response.getStatusCode().value() == HttpStatus.Series.CLIENT_ERROR.value() ||
                    response.getStatusCode().value() == HttpStatus.Series.SERVER_ERROR.value();
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            // Log error or handle it based on response status
            LOGGER.error("---> Error: " + response.getStatusCode() + " " + response.getStatusText());
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        // Tạo PoolingHttpClientConnectionManager với cấu hình
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100); // Duy trì tối đa 100 kết nối HTTP cùng lúc
        connectionManager.setDefaultMaxPerRoute(20); // Route có thể có tối đa 20 kết nối đồng thời

        // Tạo HttpClientBuilder với cấu hình
        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                //.setDefaultCredentialsProvider(credentialsProvider)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(TimeValue.ofMilliseconds(10000).toTimeout()) // Thời gian timeout khi kết nối
                        .setResponseTimeout(TimeValue.ofMilliseconds(10000).toTimeout()) // Thời gian timeout khi đọc dữ liệu
                        .build())
                .build();

        // Tạo HttpComponentsClientHttpRequestFactory với HttpClient
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        // Tạo RestTemplate và cấu hình các Interceptor và Error Handler
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setInterceptors(Collections.singletonList(new LoggingInterceptor()));
        restTemplate.setErrorHandler(new CustomErrorHandler());

        return restTemplate;
    }

}
*/
