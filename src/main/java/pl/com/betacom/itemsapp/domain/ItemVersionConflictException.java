package pl.com.betacom.itemsapp.domain;

public final class ItemVersionConflictException extends ConflictException {

    public static final String EX_MSG = "Current version is %d";

    public ItemVersionConflictException(Integer currentVersion) {
        super(String.format(EX_MSG, currentVersion));
    }
}
