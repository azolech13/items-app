package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.com.betacom.itemsapp.application.port.in.LoginCommand;
import pl.com.betacom.itemsapp.application.port.in.LoginResult;
import pl.com.betacom.itemsapp.application.port.in.LoginUser;

@RestController
@RequiredArgsConstructor
class LoginRestController {

    private final LoginUser loginUser;

    @PostMapping("/login")
    LoginResponse login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = loginUser.login(new LoginCommand(request.login(), request.password()));
        return new LoginResponse(result.token(), result.expiresIn());
    }
}
