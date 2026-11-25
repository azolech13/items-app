package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ItemPermissionJpaRepository extends JpaRepository<ItemPermissionEntity, UUID> {

    @Query("""
            select p.role from ItemPermissionEntity p
            where p.itemId = :itemId and p.userId = :userId
            """)
    Optional<ItemPermissionRole> findByItemIdAndUserId(@Param("itemId") UUID itemId, @Param("userId") UUID userId);
}
