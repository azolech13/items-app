package pl.com.betacom.itemsapp.infrastructure.adapter.in.security

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification

import static pl.com.betacom.itemsapp.UserTestConstants.ID

class SecurityCurrentUserProviderSpec extends Specification {

    SecurityCurrentUserProvider provider = new SecurityCurrentUserProvider()

    def cleanup() {
        SecurityContextHolder.clearContext()
    }

    def "reads the user id from the security context"() {
        given:
        SecurityContextHolder.context.authentication =
                new UsernamePasswordAuthenticationToken(ID, null, List.of())

        expect:
        provider.id() == ID
    }
}
