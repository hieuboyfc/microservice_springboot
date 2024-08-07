package com.zimji.gateway.api;

import com.zimji.gateway.payload.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackAPI {

    @GetMapping
    public ResponseEntity<?> fallback() {
        BaseResponse<?> baseResponse = BaseResponse.builder()
                .result("Service is temporarily unavailable. Please try again later.")
                .build();
        return ResponseEntity.ok(baseResponse);
    }

}
