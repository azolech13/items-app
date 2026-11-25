package pl.com.betacom.itemsapp.domain;

public final class ItemEditDeniedException extends ItemAccessDeniedException {

    public static final String EX_MSG = "No permission to edit this item";

    public ItemEditDeniedException() {
        super(EX_MSG);
    }
}
