package pl.com.betacom.itemsapp.application.service

import pl.com.betacom.itemsapp.application.port.in.LoginResult
import pl.com.betacom.itemsapp.application.port.out.PasswordHasher
import pl.com.betacom.itemsapp.application.port.out.UserRepository
import pl.com.betacom.itemsapp.domain.InvalidCredentialsException
import spock.lang.Specification

import static pl.com.betacom.itemsapp.UserTestConstants.EXPIRES_IN
import static pl.com.betacom.itemsapp.UserTestConstants.LOGIN
import static pl.com.betacom.itemsapp.UserTestConstants.PASSWORD
import static pl.com.betacom.itemsapp.UserTestConstants.PASSWORD_HASH
import static pl.com.betacom.itemsapp.UserTestConstants.TOKEN
import static pl.com.betacom.itemsapp.UserTestConstants.loginCommand
import static pl.com.betacom.itemsapp.UserTestConstants.user

class LoginUserServiceSpec extends Specification {

    UserRepository userRepository = Mock()
    PasswordHasher passwordHasher = Mock()
    LoginResultMapper loginResultMapper = Mock()
    LoginUserService service = new LoginUserService(userRepository, passwordHasher, loginResultMapper)

    def "rejects an unknown login without checking the password"() {
        when:
        service.login(loginCommand())

        then:
        1 * userRepository.findByLogin(LOGIN) >> Optional.empty()
        thrown(InvalidCredentialsException)
        0 * passwordHasher.matches(_, _)
        0 * loginResultMapper.toResult(_)
    }

    def "rejects a wrong password"() {
        when:
        service.login(loginCommand())

        then:
        1 * userRepository.findByLogin(LOGIN) >> Optional.of(user())
        1 * passwordHasher.matches(PASSWORD, PASSWORD_HASH) >> false
        thrown(InvalidCredentialsException)
        0 * loginResultMapper.toResult(_)
    }

    def "returns the result built by the mapper"() {
        given:
        def result = new LoginResult(TOKEN, EXPIRES_IN)

        when:
        def loggedIn = service.login(loginCommand())

        then:
        1 * userRepository.findByLogin(LOGIN) >> Optional.of(user())
        1 * passwordHasher.matches(PASSWORD, PASSWORD_HASH) >> true
        1 * loginResultMapper.toResult(user()) >> result
        loggedIn == result
    }
}
