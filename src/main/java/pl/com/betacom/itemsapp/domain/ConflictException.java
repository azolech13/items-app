package pl.com.betacom.itemsapp.domain;

public abstract sealed class ConflictException extends RuntimeException
        permits LoginTakenException, ItemVersionConflictException {

    protected ConflictException(String message) {
        super(message);
    }
}
