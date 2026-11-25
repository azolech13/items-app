package pl.com.betacom.itemsapp.application.port.out;

import pl.com.betacom.itemsapp.domain.Item;
import pl.com.betacom.itemsapp.domain.ItemRole;

public record GrantedItem(Item item, ItemRole role) {
}
