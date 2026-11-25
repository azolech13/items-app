package pl.com.betacom.itemsapp.infrastructure.adapter.in.security

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pl.com.betacom.itemsapp.application.port.out.TokenProvider
import spock.mock.DetachedMockFactory

@TestConfiguration
@EnableConfigurationProperties(LoginRateLimitProperties)
class SecurityFiltersConfiguration {

    private final mocks = new DetachedMockFactory()

    @Bean
    TokenProvider tokenProvider() {
        mocks.Mock(TokenProvider)
    }
}
