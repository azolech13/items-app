package pl.com.betacom.itemsapp.domain;

public class LoginTakenException extends RuntimeException {

    public static final String EX_MSG = "Login %s is already taken";

    public LoginTakenException(String login) {
        super(String.format(EX_MSG, login));
    }
}
