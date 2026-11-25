package pl.com.betacom.itemsapp.domain;

public final class ItemDeleteDeniedException extends ItemAccessDeniedException {

    public static final String EX_MSG = "No permission to delete this item";

    public ItemDeleteDeniedException() {
        super(EX_MSG);
    }
}
