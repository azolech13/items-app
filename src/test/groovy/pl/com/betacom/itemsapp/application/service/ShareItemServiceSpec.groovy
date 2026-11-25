package pl.com.betacom.itemsapp.application.service

import pl.com.betacom.itemsapp.application.port.in.ShareItemCommand
import pl.com.betacom.itemsapp.application.port.in.ShareResult
import pl.com.betacom.itemsapp.application.port.out.CurrentUserProvider
import pl.com.betacom.itemsapp.application.port.out.ItemPermissionRepository
import pl.com.betacom.itemsapp.application.port.out.ItemRepository
import pl.com.betacom.itemsapp.application.port.out.UserRepository
import pl.com.betacom.itemsapp.domain.ItemNotFoundException
import pl.com.betacom.itemsapp.domain.ItemPermission
import pl.com.betacom.itemsapp.domain.ItemRole
import pl.com.betacom.itemsapp.domain.ItemShareDeniedException
import pl.com.betacom.itemsapp.domain.SelfShareException
import pl.com.betacom.itemsapp.domain.UserNotFoundException
import spock.lang.Specification

import java.time.Instant

import static pl.com.betacom.itemsapp.ItemTestConstants.CREATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.GRANTED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.ITEM_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.PERMISSION_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.SHARED_OWNER_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.item
import static pl.com.betacom.itemsapp.UserTestConstants.ID
import static pl.com.betacom.itemsapp.domain.ItemShareDeniedException.EX_MSG

class ShareItemServiceSpec extends Specification {

    CurrentUserProvider currentUserProvider = Mock()
    ItemRepository itemRepository = Mock()
    ItemPermissionRepository itemPermissionRepository = Mock()
    UserRepository userRepository = Mock()
    ShareItemMapper shareItemMapper = Mock()
    ShareItemService service = new ShareItemService(
            currentUserProvider, itemRepository, itemPermissionRepository, userRepository, shareItemMapper)

    def "creates a grant for another user"() {
        given:
        def command = new ShareItemCommand(ITEM_ID, SHARED_OWNER_ID, ItemRole.EDITOR)
        def permission = permission(ItemRole.EDITOR, GRANTED_AT, GRANTED_AT)
        def created = new ShareResult(ITEM_ID, SHARED_OWNER_ID, ItemRole.EDITOR, GRANTED_AT, true)

        when:
        def result = service.share(command)

        then:
        1 * currentUserProvider.id() >> ID
        1 * itemRepository.findById(ITEM_ID) >> Optional.of(item())
        1 * userRepository.existsById(SHARED_OWNER_ID) >> true
        1 * itemPermissionRepository.findByItemAndUser(ITEM_ID, SHARED_OWNER_ID) >> Optional.empty()
        1 * shareItemMapper.toNew(command) >> permission
        1 * itemPermissionRepository.insert(permission) >> permission
        1 * shareItemMapper.toResult(permission, true) >> created
        0 * itemPermissionRepository.update(_)
        result == created
    }

    def "overwrites an existing role"() {
        given:
        def command = new ShareItemCommand(ITEM_ID, SHARED_OWNER_ID, ItemRole.EDITOR)
        def existing = permission(ItemRole.VIEWER, CREATED_AT, CREATED_AT)
        def updated = permission(ItemRole.EDITOR, CREATED_AT, GRANTED_AT)
        def overwritten = new ShareResult(ITEM_ID, SHARED_OWNER_ID, ItemRole.EDITOR, GRANTED_AT, false)

        when:
        def result = service.share(command)

        then:
        1 * currentUserProvider.id() >> ID
        1 * itemRepository.findById(ITEM_ID) >> Optional.of(item())
        1 * userRepository.existsById(SHARED_OWNER_ID) >> true
        1 * itemPermissionRepository.findByItemAndUser(ITEM_ID, SHARED_OWNER_ID) >> Optional.of(existing)
        1 * shareItemMapper.toUpdated(existing, ItemRole.EDITOR) >> updated
        1 * itemPermissionRepository.update(updated) >> updated
        1 * shareItemMapper.toResult(updated, false) >> overwritten
        0 * itemPermissionRepository.insert(_)
        result == overwritten
    }

    def "rejects granting a role to the owner"() {
        when:
        service.share(new ShareItemCommand(ITEM_ID, ID, ItemRole.EDITOR))

        then:
        1 * currentUserProvider.id() >> ID
        1 * itemRepository.findById(ITEM_ID) >> Optional.of(item())
        0 * userRepository.existsById(_)
        0 * itemPermissionRepository.findByItemAndUser(_, _)
        0 * shareItemMapper._
        thrown(SelfShareException)
    }

    def "rejects a user who is not the owner"() {
        when:
        service.share(new ShareItemCommand(ITEM_ID, ID, ItemRole.VIEWER))

        then:
        1 * currentUserProvider.id() >> SHARED_OWNER_ID
        1 * itemRepository.findById(ITEM_ID) >> Optional.of(item())
        0 * userRepository.existsById(_)
        0 * itemPermissionRepository.findByItemAndUser(_, _)
        0 * shareItemMapper._
        def denied = thrown(ItemShareDeniedException)
        denied.message == EX_MSG
    }

    def "rejects a missing item"() {
        when:
        service.share(new ShareItemCommand(ITEM_ID, SHARED_OWNER_ID, ItemRole.VIEWER))

        then:
        1 * currentUserProvider.id() >> ID
        1 * itemRepository.findById(ITEM_ID) >> Optional.empty()
        0 * itemPermissionRepository.findByItemAndUser(_, _)
        0 * shareItemMapper._
        thrown(ItemNotFoundException)
    }

    def "rejects a missing user"() {
        when:
        service.share(new ShareItemCommand(ITEM_ID, SHARED_OWNER_ID, ItemRole.VIEWER))

        then:
        1 * currentUserProvider.id() >> ID
        1 * itemRepository.findById(ITEM_ID) >> Optional.of(item())
        1 * userRepository.existsById(SHARED_OWNER_ID) >> false
        0 * itemPermissionRepository.findByItemAndUser(_, _)
        0 * shareItemMapper._
        thrown(UserNotFoundException)
    }

    private static ItemPermission permission(ItemRole role, Instant createdAt, Instant updatedAt) {
        new ItemPermission(PERMISSION_ID, ITEM_ID, SHARED_OWNER_ID, role, createdAt, updatedAt)
    }
}
