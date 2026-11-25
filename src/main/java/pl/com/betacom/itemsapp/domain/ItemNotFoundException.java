package pl.com.betacom.itemsapp.domain;

import java.util.UUID;

public final class ItemNotFoundException extends NotFoundException {

    public static final String EX_MSG = "Item %s was not found";

    public ItemNotFoundException(UUID id) {
        super(String.format(EX_MSG, id));
    }
}
