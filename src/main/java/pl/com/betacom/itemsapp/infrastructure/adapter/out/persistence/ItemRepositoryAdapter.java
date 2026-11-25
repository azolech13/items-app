package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
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
