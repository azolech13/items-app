package pl.com.betacom.itemsapp.application.port.out;

import java.time.Instant;
import java.util.UUID;
import pl.com.betacom.itemsapp.domain.ItemRevisionType;

public record ItemRevision(
        int revision,
        ItemRevisionType revisionType,
        Instant timestamp,
        UUID changedBy,
        String title,
        String content) {
}
