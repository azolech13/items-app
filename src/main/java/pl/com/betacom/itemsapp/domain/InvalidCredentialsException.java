package pl.com.betacom.itemsapp.domain;

public class InvalidCredentialsException extends RuntimeException {

    public static final String EX_MSG = "Invalid login or password";

    public InvalidCredentialsException() {
        super(EX_MSG);
    }
}
