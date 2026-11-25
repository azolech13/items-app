package pl.com.betacom.itemsapp.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.betacom.itemsapp.application.port.in.RegisterUser;
import pl.com.betacom.itemsapp.application.port.in.RegisterUserCommand;
import pl.com.betacom.itemsapp.application.port.out.UserRepository;
import pl.com.betacom.itemsapp.domain.LoginTakenException;
import pl.com.betacom.itemsapp.domain.User;

@Service
@RequiredArgsConstructor
@Slf4j
class RegisterUserService implements RegisterUser {

    private final UserRepository userRepository;
    private final RegisterUserMapper registerUserMapper;

    @Override
    @Transactional
    public User register(RegisterUserCommand command) {
        checkLoginIsTaken(command);
        User saved = userRepository.save(registerUserMapper.toUser(command));
        log.info("Registered user {} with id {}", saved.login(), saved.id());
        return saved;
    }

    private void checkLoginIsTaken(RegisterUserCommand command) {
        if (userRepository.existsByLogin(command.login())) {
            log.warn("Registration rejected, login already taken: {}", command.login());
            throw new LoginTakenException(command.login());
        }
    }
}
