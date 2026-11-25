package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.com.betacom.itemsapp.application.port.out.GrantedItem;
import pl.com.betacom.itemsapp.application.port.out.ItemRepository;
import pl.com.betacom.itemsapp.domain.Item;

@Component
@RequiredArgsConstructor
class ItemRepositoryAdapter implements ItemRepository {

    private final ItemJpaRepository items;

    @Override
    public Item save(Item item) {
        return toDomain(items.saveAndFlush(toEntity(item)));
    }

    @Override
    public List<Item> findActiveByOwner(UUID ownerId) {
        return items.findActiveByOwner(ownerId).stream()
                .map(ItemRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public List<GrantedItem> findActiveGranted(UUID userId) {
        return items.findActiveSharedWith(userId).stream()
                .map(ItemRepositoryAdapter::toGranted)
                .toList();
    }

    private static ItemEntity toEntity(Item item) {
        return new ItemEntity(
                item.id(),
                item.ownerId(),
                item.title(),
                item.content(),
                item.version(),
                item.deleted(),
                item.createdAt(),
                item.updatedAt());
    }

    private static GrantedItem toGranted(GrantedItemView view) {
        return new GrantedItem(
                new Item(
                        view.id(),
                        view.ownerId(),
                        view.title(),
                        view.content(),
                        view.version(),
                        view.deleted(),
                        view.createdAt(),
                        view.updatedAt()),
                view.role());
    }

    private static Item toDomain(ItemEntity entity) {
        return new Item(
                entity.getId(),
                entity.getOwnerId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getVersion(),
                entity.isDeleted(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
