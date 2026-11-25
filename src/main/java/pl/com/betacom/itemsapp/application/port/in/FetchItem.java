package pl.com.betacom.itemsapp.application.port.in;

import java.time.Instant;
import java.util.UUID;
import pl.com.betacom.itemsapp.domain.ItemRole;

public record FetchItem(
        UUID id,
        String title,
        String content,
        Integer version,
        UUID ownerId,
        ItemRole myRole,
        Instant updatedAt) {
}
