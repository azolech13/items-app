package pl.com.betacom.itemsapp.application.port.in;

import java.time.Instant;
import pl.com.betacom.itemsapp.domain.ItemRevisionType;

public record ItemHistory(
        int revision,
        ItemRevisionType revisionType,
        Instant timestamp,
        String changedBy,
        String title,
        String content) {
}
