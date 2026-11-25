package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import java.time.Instant;
import java.util.UUID;
import pl.com.betacom.itemsapp.domain.ItemRole;

record ShareItemResponse(UUID itemId, UUID userId, ItemRole role, Instant grantedAt) {
}
