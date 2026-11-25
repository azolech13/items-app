package pl.com.betacom.itemsapp.application.port.out;

import pl.com.betacom.itemsapp.domain.Item;

public interface ItemRepository {

    Item save(Item item);
}
