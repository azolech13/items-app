package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.com.betacom.itemsapp.application.port.out.ItemPermissionRepository;
import pl.com.betacom.itemsapp.domain.ItemNotFoundException;
import pl.com.betacom.itemsapp.domain.ItemPermission;
import pl.com.betacom.itemsapp.domain.ItemPermissionNotFoundException;
import pl.com.betacom.itemsapp.domain.ItemRole;

@Component
@RequiredArgsConstructor
class ItemPermissionRepositoryAdapter implements ItemPermissionRepository {

    private final ItemPermissionJpaRepository permissionsRepository;

    @Override
    public Optional<ItemRole> findRole(UUID itemId, UUID userId) {
        return permissionsRepository.findRoleByItemIdAndUserId(itemId, userId)
                .map(role -> ItemRole.valueOf(role.name()));
    }

    @Override
    public Optional<ItemPermission> findByItemAndUser(UUID itemId, UUID userId) {
        return permissionsRepository.findByItemIdAndUserId(itemId, userId)
                .map(ItemPermissionRepositoryAdapter::toDomain);
    }

    @Override
    public ItemPermission insert(ItemPermission permission) {
        return toDomain(permissionsRepository.saveAndFlush(toEntity(permission)));
    }

    @Override
    public ItemPermission update(ItemPermission permission) {
        ItemPermissionEntity current = permissionsRepository.findByItemIdAndUserId(permission.itemId(), permission.userId())
                .orElseThrow(() -> new ItemNotFoundException(permission.itemId()));
        current.setRole(ItemPermissionRole.valueOf(permission.role().name()));
        current.setUpdatedAt(permission.updatedAt());
        return toDomain(permissionsRepository.saveAndFlush(current));
    }

    @Override
    public void delete(ItemPermission permission) {
        ItemPermissionEntity current = permissionsRepository.findByItemIdAndUserId(permission.itemId(), permission.userId())
                .orElseThrow(() -> new ItemPermissionNotFoundException(permission.itemId(), permission.userId()));
        permissionsRepository.delete(current);
    }

    private static ItemPermissionEntity toEntity(ItemPermission permission) {
        return new ItemPermissionEntity(
                permission.id(),
                permission.itemId(),
                permission.userId(),
                ItemPermissionRole.valueOf(permission.role().name()),
                permission.createdAt(),
                permission.updatedAt());
    }

    private static ItemPermission toDomain(ItemPermissionEntity entity) {
        return new ItemPermission(
                entity.getId(),
                entity.getItemId(),
                entity.getUserId(),
                ItemRole.valueOf(entity.getRole().name()),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
