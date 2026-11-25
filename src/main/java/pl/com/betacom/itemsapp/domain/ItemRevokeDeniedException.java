package pl.com.betacom.itemsapp.domain;

public final class ItemRevokeDeniedException extends ItemAccessDeniedException {

    public static final String EX_MSG = "No permission to revoke this item";

    public ItemRevokeDeniedException() {
        super(EX_MSG);
    }
}
