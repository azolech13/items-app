package pl.com.betacom.itemsapp.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.com.betacom.itemsapp.application.port.in.UpdateItemCommand;
import pl.com.betacom.itemsapp.application.port.out.DateTimeProvider;
import pl.com.betacom.itemsapp.domain.Item;

@Component
@RequiredArgsConstructor
class UpdateItemMapper {

    private final DateTimeProvider dateTimeProvider;

    Item apply(Item item, UpdateItemCommand command) {
        return new Item(
                item.id(),
                item.ownerId(),
                command.title() == null ? item.title() : command.title(),
                command.contentSpecified() ? command.content() : item.content(),
                item.version(),
                item.deleted(),
                item.createdAt(),
                dateTimeProvider.now());
    }
}
