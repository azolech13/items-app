package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.com.betacom.itemsapp.application.port.in.RegisterUser;
import pl.com.betacom.itemsapp.application.port.in.RegisterUserCommand;
import pl.com.betacom.itemsapp.domain.User;

@RestController
@RequiredArgsConstructor
class RegistrationRestController {

    private final RegisterUser registerUser;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = registerUser.register(new RegisterUserCommand(request.login(), request.password()));
        return new RegisterResponse(user.id(), user.login(), user.createdAt());
    }
}
