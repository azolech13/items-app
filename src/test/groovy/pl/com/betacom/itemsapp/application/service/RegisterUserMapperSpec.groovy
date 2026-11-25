package pl.com.betacom.itemsapp.application.service

import pl.com.betacom.itemsapp.application.port.out.DateTimeProvider
import pl.com.betacom.itemsapp.application.port.out.PasswordHasher
import pl.com.betacom.itemsapp.application.port.out.UuidGenerator
import spock.lang.Specification

import static pl.com.betacom.itemsapp.UserTestConstants.CREATED_AT
import static pl.com.betacom.itemsapp.UserTestConstants.ID
import static pl.com.betacom.itemsapp.UserTestConstants.LOGIN
import static pl.com.betacom.itemsapp.UserTestConstants.PASSWORD
import static pl.com.betacom.itemsapp.UserTestConstants.PASSWORD_HASH
import static pl.com.betacom.itemsapp.UserTestConstants.command

class RegisterUserMapperSpec extends Specification {

    UuidGenerator uuidGenerator = Mock()
    DateTimeProvider dateTimeProvider = Mock()
    PasswordHasher passwordHasher = Mock()
    RegisterUserMapper mapper = new RegisterUserMapper(uuidGenerator, dateTimeProvider, passwordHasher)

    def "builds a user from the generators and the hashed password"() {
        when:
        def user = mapper.toUser(command())

        then:
        1 * uuidGenerator.generate() >> ID
        1 * dateTimeProvider.now() >> CREATED_AT
        1 * passwordHasher.hash(PASSWORD) >> PASSWORD_HASH
        user.id() == ID
        user.login() == LOGIN
        user.passwordHash() == PASSWORD_HASH
        user.passwordHash() != PASSWORD
        user.createdAt() == CREATED_AT
    }
}
