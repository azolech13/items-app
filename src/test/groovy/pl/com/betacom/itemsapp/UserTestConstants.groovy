package pl.com.betacom.itemsapp

import pl.com.betacom.itemsapp.application.port.in.RegisterUserCommand
import pl.com.betacom.itemsapp.domain.User

import java.time.Instant

class UserTestConstants {

    static final UUID ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
    static final Instant CREATED_AT = Instant.parse("2025-06-01T10:00:00Z")
    static final String LOGIN = "jan.kowalski"
    static final String PASSWORD = "password1"
    static final String PASSWORD_HASH = "hashed"
    static final String REGISTER_BODY = '{"login":"' + LOGIN + '","password":"' + PASSWORD + '"}'

    static RegisterUserCommand command() {
        new RegisterUserCommand(LOGIN, PASSWORD)
    }

    static User user() {
        new User(ID, LOGIN, PASSWORD_HASH, CREATED_AT)
    }
}
