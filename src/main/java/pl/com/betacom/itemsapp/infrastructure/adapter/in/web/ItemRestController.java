package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.com.betacom.itemsapp.application.port.in.CreateItem;
import pl.com.betacom.itemsapp.application.port.in.CreateItemCommand;
import pl.com.betacom.itemsapp.domain.Item;

@RestController
@RequiredArgsConstructor
class ItemRestController {

    private final CreateItem createItem;

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    ItemResponse create(@Valid @RequestBody ItemRequest request) {
        Item item = createItem.create(new CreateItemCommand(request.title(), request.content()));
        return createItemResponse(item);
    }

    @NonNull
    private static ItemResponse createItemResponse(Item item) {
        return new ItemResponse(
                item.id(),
                item.title(),
                item.content(),
                item.version(),
                item.ownerId(),
                item.createdAt(),
                item.updatedAt());
    }
}
