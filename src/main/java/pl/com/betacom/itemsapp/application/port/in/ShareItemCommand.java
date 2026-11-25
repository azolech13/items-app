package pl.com.betacom.itemsapp.application.port.in;

import java.util.UUID;
import pl.com.betacom.itemsapp.domain.ItemRole;

public record ShareItemCommand(UUID itemId, UUID userId, ItemRole role) {
}
