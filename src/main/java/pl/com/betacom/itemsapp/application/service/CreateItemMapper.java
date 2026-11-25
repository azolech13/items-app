package pl.com.betacom.itemsapp.application.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.com.betacom.itemsapp.application.port.in.CreateItemCommand;
import pl.com.betacom.itemsapp.application.port.out.CurrentUserProvider;
import pl.com.betacom.itemsapp.application.port.out.DateTimeProvider;
import pl.com.betacom.itemsapp.application.port.out.UuidGenerator;
import pl.com.betacom.itemsapp.domain.Item;

@Component
@RequiredArgsConstructor
class CreateItemMapper {

    private final UuidGenerator uuidGenerator;
    private final DateTimeProvider dateTimeProvider;
    private final CurrentUserProvider currentUserProvider;

    Item toItem(CreateItemCommand command) {
        Instant now = dateTimeProvider.now();
        return new Item(
                uuidGenerator.generate(),
                currentUserProvider.id(),
                command.title(),
                command.content(),
                null,
                false,
                now,
                now);
    }
}
