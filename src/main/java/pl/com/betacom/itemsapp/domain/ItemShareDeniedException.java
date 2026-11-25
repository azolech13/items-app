package pl.com.betacom.itemsapp.domain;

public final class ItemShareDeniedException extends ItemAccessDeniedException {

    public static final String EX_MSG = "No permission to share this item";

    public ItemShareDeniedException() {
        super(EX_MSG);
    }
}
