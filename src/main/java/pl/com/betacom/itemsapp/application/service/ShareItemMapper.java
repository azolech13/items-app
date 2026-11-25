package pl.com.betacom.itemsapp.application.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.com.betacom.itemsapp.application.port.in.ShareItemCommand;
import pl.com.betacom.itemsapp.application.port.in.ShareResult;
import pl.com.betacom.itemsapp.application.port.out.DateTimeProvider;
import pl.com.betacom.itemsapp.application.port.out.UuidGenerator;
import pl.com.betacom.itemsapp.domain.ItemPermission;
import pl.com.betacom.itemsapp.domain.ItemRole;

@Component
@RequiredArgsConstructor
class ShareItemMapper {

    private final UuidGenerator uuidGenerator;
    private final DateTimeProvider dateTimeProvider;

    ItemPermission toNew(ShareItemCommand command) {
        Instant now = dateTimeProvider.now();
        return new ItemPermission(
                uuidGenerator.generate(),
                command.itemId(),
                command.userId(),
                command.role(),
                now,
                now);
    }

    ItemPermission toUpdated(ItemPermission existing, ItemRole role) {
        return new ItemPermission(
                existing.id(),
                existing.itemId(),
                existing.userId(),
                role,
                existing.createdAt(),
                dateTimeProvider.now());
    }

    ShareResult toResult(ItemPermission permission, boolean created) {
        return new ShareResult(
                permission.itemId(),
                permission.userId(),
                permission.role(),
                permission.updatedAt(),
                created);
    }
}
