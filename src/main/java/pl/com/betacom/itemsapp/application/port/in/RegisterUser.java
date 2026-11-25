package pl.com.betacom.itemsapp.application.port.in;

import pl.com.betacom.itemsapp.domain.User;

public interface RegisterUser {

    User register(RegisterUserCommand command);
}
