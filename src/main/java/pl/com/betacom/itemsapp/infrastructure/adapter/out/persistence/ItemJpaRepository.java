package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ItemJpaRepository extends JpaRepository<ItemEntity, UUID> {

    Optional<ItemEntity> findByIdAndDeleted(UUID id, boolean deleted);

    @Query("""
            select i from ItemEntity i
            where i.ownerId = :ownerId and i.deleted = false
            order by i.updatedAt desc
            """)
    List<ItemEntity> findActiveByOwner(@Param("ownerId") UUID ownerId);

    @Query("""
            select new pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence.GrantedItemView(
                i.id, i.ownerId, i.title, i.content, i.version, i.deleted, i.createdAt, i.updatedAt, p.role)
            from ItemEntity i
            join ItemPermissionEntity p on p.itemId = i.id
            where p.userId = :userId
              and i.deleted = false
              and i.ownerId <> :userId
            order by i.updatedAt desc
            """)
    List<GrantedItemView> findActiveSharedWith(@Param("userId") UUID userId);
}
