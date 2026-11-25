package pl.com.betacom.itemsapp.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.com.betacom.itemsapp.application.port.in.RegisterUserCommand;
import pl.com.betacom.itemsapp.application.port.out.PasswordHasher;
import pl.com.betacom.itemsapp.application.port.out.DateTimeProvider;
import pl.com.betacom.itemsapp.application.port.out.UuidGenerator;
import pl.com.betacom.itemsapp.domain.User;

@Component
@RequiredArgsConstructor
class RegisterUserMapper {

    private final UuidGenerator uuidGenerator;
    private final DateTimeProvider dateTimeProvider;
    private final PasswordHasher passwordHasher;

    User toUser(RegisterUserCommand command) {
        return new User(
                uuidGenerator.generate(),
                command.login(),
                passwordHasher.hash(command.password()),
                dateTimeProvider.now());
    }
}
