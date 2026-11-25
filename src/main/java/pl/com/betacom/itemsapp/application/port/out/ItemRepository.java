package pl.com.betacom.itemsapp.application.port.out;

import java.util.List;
import java.util.UUID;
import pl.com.betacom.itemsapp.domain.Item;

public interface ItemRepository {

    Item save(Item item);

    List<Item> findActiveByOwner(UUID ownerId);

    List<GrantedItem> findActiveGranted(UUID userId);
}
