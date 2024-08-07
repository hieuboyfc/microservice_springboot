package com.zimji.gateway.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackAPI {

    @GetMapping
    public String fallback() {
        return "Service is temporarily unavailable. Please try again later.";
    }

}
