package pl.com.betacom.itemsapp.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.com.betacom.itemsapp.application.port.in.LoginResult;
import pl.com.betacom.itemsapp.application.port.out.TokenProvider;
import pl.com.betacom.itemsapp.domain.User;

@Component
@RequiredArgsConstructor
class LoginResultMapper {

    private final TokenProvider tokenProvider;

    LoginResult toResult(User user) {
        return new LoginResult(tokenProvider.issue(user.id()), tokenProvider.expirationSeconds());
    }
}
