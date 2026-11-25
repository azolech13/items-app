package pl.com.betacom.itemsapp.application.service

import pl.com.betacom.itemsapp.application.port.out.TokenProvider
import spock.lang.Specification

import static pl.com.betacom.itemsapp.UserTestConstants.EXPIRES_IN
import static pl.com.betacom.itemsapp.UserTestConstants.ID
import static pl.com.betacom.itemsapp.UserTestConstants.TOKEN
import static pl.com.betacom.itemsapp.UserTestConstants.user

class LoginResultMapperSpec extends Specification {

    TokenProvider tokenProvider = Mock()
    LoginResultMapper mapper = new LoginResultMapper(tokenProvider)

    def "builds a login result from the issued token"() {
        when:
        def result = mapper.toResult(user())

        then:
        1 * tokenProvider.issue(ID) >> TOKEN
        1 * tokenProvider.expirationSeconds() >> EXPIRES_IN
        result.token() == TOKEN
        result.expiresIn() == EXPIRES_IN
    }
}
