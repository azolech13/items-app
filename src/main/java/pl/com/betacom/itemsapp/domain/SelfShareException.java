package pl.com.betacom.itemsapp.domain;

public class SelfShareException extends RuntimeException {

    public static final String EX_MSG = "Cannot grant access to yourself";

    public SelfShareException() {
        super(EX_MSG);
    }
}
