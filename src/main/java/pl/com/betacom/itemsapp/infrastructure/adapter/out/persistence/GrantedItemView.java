package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;
import pl.com.betacom.itemsapp.domain.ItemRole;

public record GrantedItemView(
        UUID id,
        UUID ownerId,
        String title,
        String content,
        Integer version,
        boolean deleted,
        Instant createdAt,
        Instant updatedAt,
        ItemRole role) {
}
