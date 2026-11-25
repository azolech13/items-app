package pl.com.betacom.itemsapp.domain;

public final class ItemHistoryDeniedException extends ItemAccessDeniedException {

    public static final String EX_MSG = "No permission to read this item";

    public ItemHistoryDeniedException() {
        super(EX_MSG);
    }
}
