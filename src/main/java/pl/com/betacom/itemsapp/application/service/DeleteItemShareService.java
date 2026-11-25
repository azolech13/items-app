package pl.com.betacom.itemsapp.application.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.betacom.itemsapp.application.port.in.DeleteItemShare;
import pl.com.betacom.itemsapp.application.port.in.DeleteItemShareCommand;
import pl.com.betacom.itemsapp.application.port.out.CurrentUserProvider;
import pl.com.betacom.itemsapp.application.port.out.ItemPermissionRepository;
import pl.com.betacom.itemsapp.application.port.out.ItemRepository;
import pl.com.betacom.itemsapp.domain.Item;
import pl.com.betacom.itemsapp.domain.ItemNotFoundException;
import pl.com.betacom.itemsapp.domain.ItemPermission;
import pl.com.betacom.itemsapp.domain.ItemPermissionNotFoundException;
import pl.com.betacom.itemsapp.domain.ItemRevokeDeniedException;

@Service
@RequiredArgsConstructor
@Slf4j
class DeleteItemShareService implements DeleteItemShare {

    private final CurrentUserProvider currentUserProvider;
    private final ItemRepository itemRepository;
    private final ItemPermissionRepository itemPermissionRepository;

    @Override
    @Transactional
    public void delete(DeleteItemShareCommand command) {
        UUID ownerId = currentUserProvider.id();
        Item item = getItem(command.itemId());
        checkOwner(item, ownerId);
        itemPermissionRepository.delete(getPermission(command));
        log.info("Revoked access to item {} from {}", command.itemId(), command.userId());
    }

    private void checkOwner(Item item, UUID ownerId) {
        if (!ownerId.equals(item.ownerId())) {
            throw new ItemRevokeDeniedException();
        }
    }

    @NonNull
    private ItemPermission getPermission(DeleteItemShareCommand command) {
        return itemPermissionRepository.findByItemAndUser(command.itemId(), command.userId())
                .orElseThrow(() -> new ItemPermissionNotFoundException(command.itemId(), command.userId()));
    }

    @NonNull
    private Item getItem(UUID id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
    }
}
