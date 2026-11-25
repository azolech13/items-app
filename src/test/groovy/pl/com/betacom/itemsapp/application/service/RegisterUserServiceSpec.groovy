package pl.com.betacom.itemsapp.application.service

import pl.com.betacom.itemsapp.application.port.out.UserRepository
import pl.com.betacom.itemsapp.domain.LoginTakenException
import spock.lang.Specification

import static pl.com.betacom.itemsapp.UserTestConstants.LOGIN
import static pl.com.betacom.itemsapp.UserTestConstants.command
import static pl.com.betacom.itemsapp.UserTestConstants.user

class RegisterUserServiceSpec extends Specification {

    UserRepository userRepository = Mock()
    RegisterUserMapper registerUserMapper = Mock()
    RegisterUserService service = new RegisterUserService(userRepository, registerUserMapper)

    def "rejects a taken login before creating a user"() {
        when:
        service.register(command())

        then:
        1 * userRepository.existsByLogin(LOGIN) >> true
        thrown(LoginTakenException)
        0 * registerUserMapper.toUser(_)
        0 * userRepository.save(_)
    }

    def "saves the user created by the mapper"() {
        given:
        def user = user()

        when:
        def saved = service.register(command())

        then:
        1 * userRepository.existsByLogin(LOGIN) >> false
        1 * registerUserMapper.toUser(command()) >> user
        1 * userRepository.save(user) >> user
        saved == user
    }
}
