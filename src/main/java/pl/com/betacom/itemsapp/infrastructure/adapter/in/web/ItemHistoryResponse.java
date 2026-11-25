package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import java.time.Instant;
import pl.com.betacom.itemsapp.domain.ItemRevisionType;

record ItemHistoryResponse(
        int revision,
        ItemRevisionType revisionType,
        Instant timestamp,
        String changedBy,
        String title,
        String content) {
}
