package pl.com.betacom.itemsapp.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.com.betacom.itemsapp.application.port.out.DateTimeProvider;
import pl.com.betacom.itemsapp.domain.Item;

@Component
@RequiredArgsConstructor
class DeleteItemMapper {

    private final DateTimeProvider dateTimeProvider;

    Item apply(Item item) {
        return new Item(
                item.id(),
                item.ownerId(),
                item.title(),
                item.content(),
                item.version(),
                true,
                item.createdAt(),
                dateTimeProvider.now());
    }
}
