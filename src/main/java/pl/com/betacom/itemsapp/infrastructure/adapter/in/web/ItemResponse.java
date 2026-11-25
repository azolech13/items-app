package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import java.time.Instant;
import java.util.UUID;

record ItemResponse(
        UUID id,
        String title,
        String content,
        Integer version,
        UUID ownerId,
        Instant createdAt,
        Instant updatedAt) {
}
