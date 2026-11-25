package pl.com.betacom.itemsapp.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import pl.com.betacom.itemsapp.domain.Item;

public interface ItemRepository {

    Item save(Item item);

    Item update(Item item);

    Optional<Item> findById(UUID id);

    List<Item> findActiveByOwner(UUID ownerId);

    List<GrantedItem> findActiveGranted(UUID userId);
}
