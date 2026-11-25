package pl.com.betacom.itemsapp.application.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.betacom.itemsapp.application.port.in.FetchItemHistory;
import pl.com.betacom.itemsapp.application.port.in.ItemHistory;
import pl.com.betacom.itemsapp.application.port.out.CurrentUserProvider;
import pl.com.betacom.itemsapp.application.port.out.ItemHistoryRepository;
import pl.com.betacom.itemsapp.application.port.out.ItemPermissionRepository;
import pl.com.betacom.itemsapp.application.port.out.ItemRepository;
import pl.com.betacom.itemsapp.application.port.out.ItemRevision;
import pl.com.betacom.itemsapp.application.port.out.UserRepository;
import pl.com.betacom.itemsapp.domain.Item;
import pl.com.betacom.itemsapp.domain.ItemHistoryDeniedException;
import pl.com.betacom.itemsapp.domain.ItemNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
class FetchItemHistoryService implements FetchItemHistory {

    private final CurrentUserProvider currentUserProvider;
    private final ItemRepository itemRepository;
    private final ItemPermissionRepository itemPermissionRepository;
    private final ItemHistoryRepository itemHistoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ItemHistory> fetch(UUID id) {
        UUID userId = currentUserProvider.id();
        Item item = getItem(id);
        checkAccess(item, userId);
        List<ItemHistory> history = itemHistoryRepository.findByItemId(id).stream()
                .map(this::toHistory)
                .toList();
        log.info("Fetched {} revisions of item {} for {}", history.size(), id, userId);
        return history;
    }

    private ItemHistory toHistory(ItemRevision revision) {
        return new ItemHistory(
                revision.revision(),
                revision.revisionType(),
                revision.timestamp(),
                getLogin(revision.changedBy()),
                revision.title(),
                revision.content());
    }

    private String getLogin(UUID userId) {
        return userRepository.findById(userId).login();
    }

    private void checkAccess(Item item, UUID userId) {
        if (userId.equals(item.ownerId())) {
            return;
        }
        if (hasNoRoleForItem(item, userId)) {
            throw new ItemHistoryDeniedException();
        }
    }

    private boolean hasNoRoleForItem(Item item, UUID userId) {
        return itemPermissionRepository.findRole(item.id(), userId).isEmpty();
    }

    @NonNull
    private Item getItem(UUID id) {
        return itemRepository.findIncludingDeleted(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
    }
}
