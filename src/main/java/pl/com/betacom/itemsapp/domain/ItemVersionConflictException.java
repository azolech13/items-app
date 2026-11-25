package pl.com.betacom.itemsapp.domain;

public class ItemVersionConflictException extends RuntimeException {

    public static final String EX_MSG = "Current version is %d";

    private final Integer currentVersion;

    public ItemVersionConflictException(Integer currentVersion) {
        super(String.format(EX_MSG, currentVersion));
        this.currentVersion = currentVersion;
    }

    public Integer currentVersion() {
        return currentVersion;
    }
}
