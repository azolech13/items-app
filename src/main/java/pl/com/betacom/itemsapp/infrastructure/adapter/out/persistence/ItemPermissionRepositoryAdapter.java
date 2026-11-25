package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.com.betacom.itemsapp.application.port.out.ItemPermissionRepository;
import pl.com.betacom.itemsapp.domain.ItemRole;

@Component
@RequiredArgsConstructor
class ItemPermissionRepositoryAdapter implements ItemPermissionRepository {

    private final ItemPermissionJpaRepository permissionsRepository;

    @Override
    public Optional<ItemRole> findRole(UUID itemId, UUID userId) {
        return permissionsRepository.findByItemIdAndUserId(itemId, userId)
                .map(role -> ItemRole.valueOf(role.name()));
    }
}
