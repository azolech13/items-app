package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import java.time.Instant;
import java.util.UUID;
import pl.com.betacom.itemsapp.domain.ItemRole;

record ItemListResponse(
        UUID id,
        String title,
        String content,
        Integer version,
        UUID ownerId,
        ItemRole myRole,
        Instant updatedAt) {
}
