package com.zimji.gateway.configuration;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Component
@Configuration
@ConfigurationProperties(prefix = "gateway")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GatewayProperties {

    String apiPrefix;
    String publicEndPoints;

    AuthServiceInfo authServiceInfo = new AuthServiceInfo();

    @Setter
    @Getter
    public static class AuthServiceInfo {
        private String name;
        private String url;
        private String path;
    }

}
