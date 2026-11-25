package pl.com.betacom.itemsapp.application.port.in;

import pl.com.betacom.itemsapp.domain.Item;

public interface CreateItem {

    Item create(CreateItemCommand command);
}
