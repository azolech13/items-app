package pl.com.betacom.itemsapp.application.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.betacom.itemsapp.application.port.in.ShareItem;
import pl.com.betacom.itemsapp.application.port.in.ShareItemCommand;
import pl.com.betacom.itemsapp.application.port.in.ShareResult;
import pl.com.betacom.itemsapp.application.port.out.CurrentUserProvider;
import pl.com.betacom.itemsapp.application.port.out.ItemPermissionRepository;
import pl.com.betacom.itemsapp.application.port.out.ItemRepository;
import pl.com.betacom.itemsapp.application.port.out.UserRepository;
import pl.com.betacom.itemsapp.domain.Item;
import pl.com.betacom.itemsapp.domain.ItemNotFoundException;
import pl.com.betacom.itemsapp.domain.ItemPermission;
import pl.com.betacom.itemsapp.domain.ItemShareDeniedException;
import pl.com.betacom.itemsapp.domain.SelfShareException;
import pl.com.betacom.itemsapp.domain.UserNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
class ShareItemService implements ShareItem {

    private final CurrentUserProvider currentUserProvider;
    private final ItemRepository itemRepository;
    private final ItemPermissionRepository itemPermissionRepository;
    private final UserRepository userRepository;
    private final ShareItemMapper shareItemMapper;

    @Override
    @Transactional
    public ShareResult share(ShareItemCommand command) {
        UUID ownerId = currentUserProvider.id();
        Item item = getItem(command);
        checkOwner(item, ownerId);
        checkNotSelf(command, ownerId);
        checkUserExists(command);
        ShareResult result = itemPermissionRepository.findByItemAndUser(command.itemId(), command.userId())
                .map(existing -> overwrite(existing, command))
                .orElseGet(() -> create(command));
        log.info("Granted {} on item {} to {}", result.role(), result.itemId(), result.userId());
        return result;
    }

    private ShareResult create(ShareItemCommand command) {
        return shareItemMapper.toResult(itemPermissionRepository.insert(shareItemMapper.toNew(command)), true);
    }

    private ShareResult overwrite(ItemPermission existing, ShareItemCommand command) {
        return shareItemMapper.toResult(itemPermissionRepository.update(shareItemMapper.toUpdated(existing, command.role())), false);
    }

    private void checkUserExists(ShareItemCommand command) {
        if (!userRepository.existsById(command.userId())) {
            throw new UserNotFoundException(command.userId());
        }
    }

    private void checkNotSelf(ShareItemCommand command, UUID ownerId) {
        if (ownerId.equals(command.userId())) {
            throw new SelfShareException();
        }
    }

    private void checkOwner(Item item, UUID ownerId) {
        if (!ownerId.equals(item.ownerId())) {
            throw new ItemShareDeniedException();
        }
    }

    @NonNull
    private Item getItem(ShareItemCommand command) {
        return itemRepository.findById(command.itemId())
                .orElseThrow(() -> new ItemNotFoundException(command.itemId()));
    }
}
