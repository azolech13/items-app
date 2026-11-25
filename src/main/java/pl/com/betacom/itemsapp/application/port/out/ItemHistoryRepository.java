package pl.com.betacom.itemsapp.application.port.out;

import java.util.List;
import java.util.UUID;

public interface ItemHistoryRepository {

    List<ItemRevision> findByItemId(UUID itemId);
}
