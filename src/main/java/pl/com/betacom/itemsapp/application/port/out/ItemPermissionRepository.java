package pl.com.betacom.itemsapp.application.port.out;

import java.util.Optional;
import java.util.UUID;
import pl.com.betacom.itemsapp.domain.ItemPermission;
import pl.com.betacom.itemsapp.domain.ItemRole;

public interface ItemPermissionRepository {

    Optional<ItemRole> findRole(UUID itemId, UUID userId);

    Optional<ItemPermission> findByItemAndUser(UUID itemId, UUID userId);

    ItemPermission insert(ItemPermission permission);

    ItemPermission update(ItemPermission permission);

    void delete(ItemPermission permission);
}
