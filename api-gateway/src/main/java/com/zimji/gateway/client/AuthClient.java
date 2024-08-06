package com.zimji.gateway.client;

import com.zimji.gateway.payload.request.IntrospectRequest;
import com.zimji.gateway.payload.response.BaseResponse;
import com.zimji.gateway.payload.response.IntrospectResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface AuthClient {

    @PostExchange(url = "/auth/introspect", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<BaseResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest request);

}
