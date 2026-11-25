package pl.com.betacom.itemsapp.domain;

public abstract sealed class ItemAccessDeniedException extends RuntimeException
        permits ItemEditDeniedException, ItemDeleteDeniedException, ItemHistoryDeniedException, ItemShareDeniedException {

    protected ItemAccessDeniedException(String message) {
        super(message);
    }
}
