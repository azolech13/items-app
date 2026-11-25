package pl.com.betacom.itemsapp.domain;

import java.time.Instant;
import java.util.UUID;

public record Item(
        UUID id,
        UUID ownerId,
        String title,
        String content,
        Integer version,
        boolean deleted,
        Instant createdAt,
        Instant updatedAt) {
}
