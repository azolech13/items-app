package pl.com.betacom.itemsapp.application.port.out;

import java.util.Optional;
import java.util.UUID;
import pl.com.betacom.itemsapp.domain.ItemRole;

public interface ItemPermissionRepository {

    Optional<ItemRole> findRole(UUID itemId, UUID userId);
}
