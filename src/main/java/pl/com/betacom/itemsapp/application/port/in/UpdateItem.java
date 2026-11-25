package pl.com.betacom.itemsapp.application.port.in;

import pl.com.betacom.itemsapp.domain.Item;

public interface UpdateItem {

    Item update(UpdateItemCommand command);
}
