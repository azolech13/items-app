package pl.com.betacom.itemsapp.infrastructure.adapter.in.security;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security.login-rate-limit")
@Getter
@Setter
class LoginRateLimitProperties {

    private int attempts;
    private Duration window;
}
