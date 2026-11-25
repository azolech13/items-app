package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ItemJpaRepository extends JpaRepository<ItemEntity, UUID> {
}
