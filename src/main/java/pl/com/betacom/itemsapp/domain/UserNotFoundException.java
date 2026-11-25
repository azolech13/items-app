package pl.com.betacom.itemsapp.domain;

import java.util.UUID;

public final class UserNotFoundException extends NotFoundException {

    public static final String EX_MSG = "User %s was not found";

    public UserNotFoundException(UUID id) {
        super(String.format(EX_MSG, id));
    }
}
