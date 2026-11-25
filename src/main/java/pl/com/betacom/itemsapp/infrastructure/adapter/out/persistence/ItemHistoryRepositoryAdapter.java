package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Component;
import pl.com.betacom.itemsapp.application.port.out.ItemHistoryRepository;
import pl.com.betacom.itemsapp.application.port.out.ItemRevision;

@Component
@RequiredArgsConstructor
class ItemHistoryRepositoryAdapter implements ItemHistoryRepository {

    private final EntityManager entityManager;

    @Override
    public List<ItemRevision> findByItemId(UUID itemId) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        return getResultList(itemId, reader).stream()
                .map(ItemRevisionMapper::toRevision)
                .toList();
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> getResultList(UUID itemId, AuditReader reader) {
        return reader.createQuery()
                .forRevisionsOfEntity(ItemEntity.class, false, true)
                .add(AuditEntity.id().eq(itemId))
                .addOrder(AuditEntity.revisionNumber().asc())
                .getResultList();
    }
}
