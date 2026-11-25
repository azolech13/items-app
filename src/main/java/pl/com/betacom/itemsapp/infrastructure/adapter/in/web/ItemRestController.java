package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.com.betacom.itemsapp.application.port.in.CreateItem;
import pl.com.betacom.itemsapp.application.port.in.DeleteItem;
import pl.com.betacom.itemsapp.application.port.in.CreateItemCommand;
import pl.com.betacom.itemsapp.application.port.in.FetchItem;
import pl.com.betacom.itemsapp.application.port.in.FetchItemHistory;
import pl.com.betacom.itemsapp.application.port.in.FetchItems;
import pl.com.betacom.itemsapp.application.port.in.ItemHistory;
import pl.com.betacom.itemsapp.application.port.in.UpdateItem;
import pl.com.betacom.itemsapp.application.port.in.UpdateItemCommand;
import pl.com.betacom.itemsapp.domain.Item;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
class ItemRestController {

    private final CreateItem createItem;
    private final FetchItems fetchItems;
    private final FetchItemHistory fetchItemHistory;
    private final UpdateItem updateItem;
    private final DeleteItem deleteItem;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ItemResponse create(@Valid @RequestBody ItemRequest request) {
        Item item = createItem.create(new CreateItemCommand(request.title(), request.content()));
        return createItemResponse(item);
    }

    @GetMapping("/{id}/history")
    List<ItemHistoryResponse> history(@PathVariable UUID id) {
        return fetchItemHistory.fetch(id).stream()
                .map(ItemRestController::toHistoryResponse)
                .toList();
    }

    @GetMapping
    List<ItemListResponse> list() {
        return fetchItems.fetch().stream()
                .map(ItemRestController::toListResponse)
                .toList();
    }

    @PatchMapping("/{id}")
    UpdateItemResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateItemRequest request) {
        Item item = updateItem.update(new UpdateItemCommand(id, request.title(), request.content(), request.version()));
        return toUpdateResponse(item);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID id) {
        deleteItem.delete(id);
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

    @NonNull
    private static UpdateItemResponse toUpdateResponse(Item item) {
        return new UpdateItemResponse(item.id(), item.title(), item.content(), item.version(), item.updatedAt());
    }

    @NonNull
    private static ItemHistoryResponse toHistoryResponse(ItemHistory revision) {
        return new ItemHistoryResponse(
                revision.revision(),
                revision.revisionType(),
                revision.timestamp(),
                revision.changedBy(),
                revision.title(),
                revision.content());
    }

    @NonNull
    private static ItemListResponse toListResponse(FetchItem item) {
        return new ItemListResponse(
                item.id(),
                item.title(),
                item.content(),
                item.version(),
                item.ownerId(),
                item.myRole(),
                item.updatedAt());
    }
}
