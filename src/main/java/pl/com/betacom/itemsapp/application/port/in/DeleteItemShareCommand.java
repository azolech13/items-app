package pl.com.betacom.itemsapp.application.port.in;

import java.util.UUID;

public record DeleteItemShareCommand(UUID itemId, UUID userId) {
}
