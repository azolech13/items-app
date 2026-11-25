package pl.com.betacom.itemsapp.application.port.in;

import java.time.Instant;
import java.util.UUID;
import pl.com.betacom.itemsapp.domain.ItemRole;

public record ShareResult(UUID itemId, UUID userId, ItemRole role, Instant grantedAt, boolean created) {
}
