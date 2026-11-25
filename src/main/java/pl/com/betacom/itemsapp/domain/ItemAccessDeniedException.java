package pl.com.betacom.itemsapp.domain;

public class ItemAccessDeniedException extends RuntimeException {

    public static final String EX_MSG = "No permission to edit this item";

    public ItemAccessDeniedException() {
        super(EX_MSG);
    }
}
