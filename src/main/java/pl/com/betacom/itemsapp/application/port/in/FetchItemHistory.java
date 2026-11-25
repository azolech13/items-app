package pl.com.betacom.itemsapp.application.port.in;

import java.util.List;
import java.util.UUID;

public interface FetchItemHistory {

    List<ItemHistory> fetch(UUID id);
}
