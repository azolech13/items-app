package pl.com.betacom.itemsapp.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.betacom.itemsapp.application.port.in.FetchItems;
import pl.com.betacom.itemsapp.application.port.in.FetchItem;
import pl.com.betacom.itemsapp.application.port.out.CurrentUserProvider;
import pl.com.betacom.itemsapp.application.port.out.ItemRepository;
import pl.com.betacom.itemsapp.domain.Item;
import pl.com.betacom.itemsapp.domain.ItemRole;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
class FetchItemsService implements FetchItems {

    private final CurrentUserProvider currentUserProvider;
    private final ItemRepository itemRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FetchItem> fetch() {
        UUID userId = currentUserProvider.id();

        List<FetchItem> items = Stream.concat(getItems(userId), getGrantedItems(userId))
                .sorted(Comparator.comparing(FetchItem::updatedAt).reversed().thenComparing(FetchItem::id))
                .toList();

        log.info("Fetched {} items for {}", items.size(), userId);
        return items;
    }

    @NonNull
    private Stream<FetchItem> getGrantedItems(UUID userId) {
        return itemRepository.findActiveGranted(userId).stream()
                .map(granted -> toFetchItem(granted.item(), granted.role()));
    }

    @NonNull
    private Stream<FetchItem> getItems(UUID userId) {
        return itemRepository.findActiveByOwner(userId).stream()
                .map(item -> toFetchItem(item, ItemRole.OWNER));
    }

    private static FetchItem toFetchItem(Item item, ItemRole myRole) {
        return new FetchItem(
                item.id(),
                item.title(),
                item.content(),
                item.version(),
                item.ownerId(),
                myRole,
                item.updatedAt());
    }
}
