package pl.com.betacom.itemsapp.domain;

import java.util.UUID;

public class ItemNotFoundException extends RuntimeException {

    public static final String EX_MSG = "Item %s was not found";

    public ItemNotFoundException(UUID id) {
        super(String.format(EX_MSG, id));
    }
}
