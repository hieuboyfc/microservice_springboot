package com.zimji.gateway.service;

import com.zimji.gateway.client.AuthClient;
import com.zimji.gateway.payload.request.IntrospectRequest;
import com.zimji.gateway.payload.response.BaseResponse;
import com.zimji.gateway.payload.response.IntrospectResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {

    AuthClient authClient;

    public Mono<BaseResponse<IntrospectResponse>> introspect(String token) {
        return authClient.introspect(
                IntrospectRequest.builder()
                        .token(token)
                        .build()
        );
    }

}
