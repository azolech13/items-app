package pl.com.betacom.itemsapp.application.service

import pl.com.betacom.itemsapp.application.port.in.DeleteItemShareCommand
import pl.com.betacom.itemsapp.application.port.out.CurrentUserProvider
import pl.com.betacom.itemsapp.application.port.out.ItemPermissionRepository
import pl.com.betacom.itemsapp.application.port.out.ItemRepository
import pl.com.betacom.itemsapp.domain.ItemNotFoundException
import pl.com.betacom.itemsapp.domain.ItemPermission
import pl.com.betacom.itemsapp.domain.ItemPermissionNotFoundException
import pl.com.betacom.itemsapp.domain.ItemRevokeDeniedException
import pl.com.betacom.itemsapp.domain.ItemRole
import spock.lang.Specification

import static pl.com.betacom.itemsapp.ItemTestConstants.CREATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.ITEM_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.PERMISSION_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.SHARED_OWNER_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.item
import static pl.com.betacom.itemsapp.UserTestConstants.ID

class DeleteItemShareServiceSpec extends Specification {

    CurrentUserProvider currentUserProvider = Mock()
    ItemRepository itemRepository = Mock()
    ItemPermissionRepository itemPermissionRepository = Mock()
    DeleteItemShareService service = new DeleteItemShareService(
            currentUserProvider, itemRepository, itemPermissionRepository)

    def "revokes a grant owned by the current user"() {
        given:
        def permission = permission()

        when:
        service.delete(new DeleteItemShareCommand(ITEM_ID, SHARED_OWNER_ID))

        then:
        1 * currentUserProvider.id() >> ID
        1 * itemRepository.findById(ITEM_ID) >> Optional.of(item())
        1 * itemPermissionRepository.findByItemAndUser(ITEM_ID, SHARED_OWNER_ID) >> Optional.of(permission)
        1 * itemPermissionRepository.delete(permission)
    }

    def "rejects a user who is not the owner"() {
        when:
        service.delete(new DeleteItemShareCommand(ITEM_ID, SHARED_OWNER_ID))

        then:
        1 * currentUserProvider.id() >> SHARED_OWNER_ID
        1 * itemRepository.findById(ITEM_ID) >> Optional.of(item())
        0 * itemPermissionRepository.findByItemAndUser(_, _)
        0 * itemPermissionRepository.delete(_)
        def denied = thrown(ItemRevokeDeniedException)
        denied.message == ItemRevokeDeniedException.EX_MSG
    }

    def "rejects a missing item"() {
        when:
        service.delete(new DeleteItemShareCommand(ITEM_ID, SHARED_OWNER_ID))

        then:
        1 * currentUserProvider.id() >> ID
        1 * itemRepository.findById(ITEM_ID) >> Optional.empty()
        0 * itemPermissionRepository.findByItemAndUser(_, _)
        thrown(ItemNotFoundException)
    }

    def "rejects a user who has no grant"() {
        when:
        service.delete(new DeleteItemShareCommand(ITEM_ID, SHARED_OWNER_ID))

        then:
        1 * currentUserProvider.id() >> ID
        1 * itemRepository.findById(ITEM_ID) >> Optional.of(item())
        1 * itemPermissionRepository.findByItemAndUser(ITEM_ID, SHARED_OWNER_ID) >> Optional.empty()
        0 * itemPermissionRepository.delete(_)
        def missing = thrown(ItemPermissionNotFoundException)
        missing.message == String.format(ItemPermissionNotFoundException.EX_MSG, SHARED_OWNER_ID, ITEM_ID)
    }

    private static ItemPermission permission() {
        new ItemPermission(PERMISSION_ID, ITEM_ID, SHARED_OWNER_ID, ItemRole.EDITOR, CREATED_AT, CREATED_AT)
    }
}
