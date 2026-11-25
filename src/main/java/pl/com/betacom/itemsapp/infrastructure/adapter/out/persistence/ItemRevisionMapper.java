package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;

import lombok.experimental.UtilityClass;
import org.hibernate.envers.RevisionType;
import pl.com.betacom.itemsapp.application.port.out.ItemRevision;
import pl.com.betacom.itemsapp.domain.ItemRevisionType;

@UtilityClass
final class ItemRevisionMapper {

    static ItemRevision toRevision(Object[] row) {
        ItemEntity entity = (ItemEntity) row[0];
        AuthorRevision revision = (AuthorRevision) row[1];
        RevisionType revisionType = (RevisionType) row[2];
        return new ItemRevision(
                revision.getId(),
                toType(revisionType, entity.isDeleted()),
                Instant.ofEpochMilli(revision.getTimestamp()),
                UUID.fromString(revision.getChangedBy()),
                entity.getTitle(),
                entity.getContent());
    }

    static ItemRevisionType toType(RevisionType revisionType, boolean deleted) {
        return switch (revisionType) {
            case ADD -> ItemRevisionType.ADD;
            case DEL -> ItemRevisionType.DEL;
            case MOD -> deleted ? ItemRevisionType.DEL : ItemRevisionType.MOD;
        };
    }
}
