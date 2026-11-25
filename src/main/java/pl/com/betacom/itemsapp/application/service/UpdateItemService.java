package pl.com.betacom.itemsapp.application.service;

import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.betacom.itemsapp.application.port.in.UpdateItem;
import pl.com.betacom.itemsapp.application.port.in.UpdateItemCommand;
import pl.com.betacom.itemsapp.application.port.out.CurrentUserProvider;
import pl.com.betacom.itemsapp.application.port.out.ItemPermissionRepository;
import pl.com.betacom.itemsapp.application.port.out.ItemRepository;
import pl.com.betacom.itemsapp.domain.Item;
import pl.com.betacom.itemsapp.domain.ItemEditDeniedException;
import pl.com.betacom.itemsapp.domain.ItemNotFoundException;
import pl.com.betacom.itemsapp.domain.ItemRole;
import pl.com.betacom.itemsapp.domain.ItemVersionConflictException;

@Service
@RequiredArgsConstructor
@Slf4j
class UpdateItemService implements UpdateItem {

    private final CurrentUserProvider currentUserProvider;
    private final ItemRepository itemRepository;
    private final ItemPermissionRepository itemPermissionRepository;
    private final UpdateItemMapper updateItemMapper;

    @Override
    @Transactional
    public Item update(UpdateItemCommand command) {
        UUID userId = currentUserProvider.id();
        Item item = getItem(command);
        checkRole(item, userId);
        checkConflict(command, item);
        Item saved = itemRepository.update(updateItemMapper.apply(item, command));
        log.info("Updated item {} by {}", saved.id(), userId);
        return saved;
    }

    private void checkConflict(UpdateItemCommand command, Item item) {
        if (!Objects.equals(item.version(), command.version())) {
            throw new ItemVersionConflictException(item.version());
        }
    }

    private boolean isOwnerOrEditor(Item item, UUID userId) {
        return userId.equals(item.ownerId()) || itemPermissionRepository.findRole(item.id(), userId)
                .filter(role -> role == ItemRole.EDITOR)
                .isPresent();
    }

    private void checkRole(Item item, UUID userId) {
        if (!isOwnerOrEditor(item, userId)) {
            throw new ItemEditDeniedException();
        }
    }

    @NonNull
    private Item getItem(UpdateItemCommand command) {
        return itemRepository.findById(command.id())
                .orElseThrow(() -> new ItemNotFoundException(command.id()));
    }

}
