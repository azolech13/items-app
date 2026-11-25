package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import pl.com.betacom.itemsapp.application.port.out.GrantedItem;
import pl.com.betacom.itemsapp.application.port.out.ItemRepository;
import pl.com.betacom.itemsapp.domain.Item;
import pl.com.betacom.itemsapp.domain.ItemNotFoundException;
import pl.com.betacom.itemsapp.domain.ItemRole;
import pl.com.betacom.itemsapp.domain.ItemVersionConflictException;

@Component
@RequiredArgsConstructor
class ItemRepositoryAdapter implements ItemRepository {

    private final ItemJpaRepository itemsRepository;

    @Override
    public Item save(Item item) {
        return toDomain(itemsRepository.saveAndFlush(toEntity(item)));
    }

    @Override
    public Item update(Item item) {
        try {
            ItemEntity current = itemsRepository.findById(item.id())
                    .orElseThrow(() -> new ItemNotFoundException(item.id()));
            current.setTitle(item.title());
            current.setContent(item.content());
            current.setUpdatedAt(item.updatedAt());
            return toDomain(itemsRepository.saveAndFlush(current));
        } catch (OptimisticLockingFailureException exception) {
            throw new ItemVersionConflictException(item.version());
        }
    }

    @Override
    public void delete(Item item) {
        ItemEntity current = itemsRepository.findById(item.id())
                .orElseThrow(() -> new ItemNotFoundException(item.id()));
        current.setDeleted(item.deleted());
        current.setUpdatedAt(item.updatedAt());
    }

    @Override
    public Optional<Item> findById(UUID id) {
        return itemsRepository.findByIdAndDeleted(id, false)
                .map(ItemRepositoryAdapter::toDomain);
    }

    @Override
    public List<Item> findActiveByOwner(UUID ownerId) {
        return itemsRepository.findActiveByOwner(ownerId).stream()
                .map(ItemRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public List<GrantedItem> findActiveGranted(UUID userId) {
        return itemsRepository.findActiveSharedWith(userId).stream()
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
                ItemRole.valueOf(view.role().name()));
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
