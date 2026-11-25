package pl.com.betacom.itemsapp.domain;

public class ItemAccessDeniedException extends RuntimeException {

    public static final String EDIT_EX_MSG = "No permission to edit this item";
    public static final String DELETE_EX_MSG = "No permission to delete this item";

    public ItemAccessDeniedException(String message) {
        super(message);
    }
}
