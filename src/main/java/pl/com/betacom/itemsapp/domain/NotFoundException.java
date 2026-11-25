package pl.com.betacom.itemsapp.domain;

public abstract sealed class NotFoundException extends RuntimeException
        permits ItemNotFoundException, UserNotFoundException {

    protected NotFoundException(String message) {
        super(message);
    }
}
