package pl.com.betacom.itemsapp.application.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.betacom.itemsapp.application.port.in.DeleteItem;
import pl.com.betacom.itemsapp.application.port.out.CurrentUserProvider;
import pl.com.betacom.itemsapp.application.port.out.ItemRepository;
import pl.com.betacom.itemsapp.domain.Item;
import pl.com.betacom.itemsapp.domain.ItemDeleteDeniedException;
import pl.com.betacom.itemsapp.domain.ItemNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
class DeleteItemService implements DeleteItem {

    private final CurrentUserProvider currentUserProvider;
    private final ItemRepository itemRepository;
    private final DeleteItemMapper deleteItemMapper;

    @Override
    @Transactional
    public void delete(UUID id) {
        UUID userId = currentUserProvider.id();
        Item item = getItem(id);
        checkIsOwner(userId, item);
        itemRepository.delete(deleteItemMapper.apply(item));
        log.info("Deleted item {} by {}", id, userId);
    }

    private void checkIsOwner(UUID userId, Item item) {
        if (!userId.equals(item.ownerId())) {
            throw new ItemDeleteDeniedException();
        }
    }

    @NonNull
    private Item getItem(UUID id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
    }
}
