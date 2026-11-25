package pl.com.betacom.itemsapp.infrastructure.adapter.out.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security.jwt")
@Getter
@Setter
class JwtProperties {

    private String secret;
    private long expirationSeconds;
}
