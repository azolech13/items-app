package pl.com.betacom.itemsapp.domain;

import java.util.UUID;

public final class ItemPermissionNotFoundException extends NotFoundException {

    public static final String EX_MSG = "Access for user %s on item %s was not found";

    public ItemPermissionNotFoundException(UUID itemId, UUID userId) {
        super(String.format(EX_MSG, userId, itemId));
    }
}
