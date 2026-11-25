package pl.com.betacom.itemsapp.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.betacom.itemsapp.application.port.in.LoginCommand;
import pl.com.betacom.itemsapp.application.port.in.LoginResult;
import pl.com.betacom.itemsapp.application.port.in.LoginUser;
import pl.com.betacom.itemsapp.application.port.out.PasswordHasher;
import pl.com.betacom.itemsapp.application.port.out.UserRepository;
import pl.com.betacom.itemsapp.domain.InvalidCredentialsException;
import pl.com.betacom.itemsapp.domain.User;

@Service
@RequiredArgsConstructor
@Slf4j
class LoginUserService implements LoginUser {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final LoginResultMapper loginResultMapper;

    @Override
    @Transactional(readOnly = true)
    public LoginResult login(LoginCommand command) {
        User user = userRepository.findByLogin(command.login())
                .filter(found -> passwordHasher.matches(command.password(), found.passwordHash()))
                .orElseThrow(() -> getInvalidCredentialsException(command));
        log.info("Login succeeded for {}", user.login());
        return loginResultMapper.toResult(user);
    }

    @NonNull
    private InvalidCredentialsException getInvalidCredentialsException(LoginCommand command) {
        log.warn("Login rejected for {}", command.login());
        return new InvalidCredentialsException();
    }
}
