package pl.com.betacom.itemsapp.domain;

import java.time.Instant;
import java.util.UUID;

public record ItemPermission(
        UUID id,
        UUID itemId,
        UUID userId,
        ItemRole role,
        Instant createdAt,
        Instant updatedAt) {
}
