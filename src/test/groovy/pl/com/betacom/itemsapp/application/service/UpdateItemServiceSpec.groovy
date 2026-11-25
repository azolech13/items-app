package pl.com.betacom.itemsapp.application.service

import pl.com.betacom.itemsapp.application.port.in.UpdateItemCommand
import pl.com.betacom.itemsapp.application.port.out.CurrentUserProvider
import pl.com.betacom.itemsapp.application.port.out.DateTimeProvider
import pl.com.betacom.itemsapp.application.port.out.ItemPermissionRepository
import pl.com.betacom.itemsapp.application.port.out.ItemRepository
import pl.com.betacom.itemsapp.domain.Item
import pl.com.betacom.itemsapp.domain.ItemAccessDeniedException
import pl.com.betacom.itemsapp.domain.ItemNotFoundException
import pl.com.betacom.itemsapp.domain.ItemRole
import pl.com.betacom.itemsapp.domain.ItemVersionConflictException
import spock.lang.Specification

import static pl.com.betacom.itemsapp.ItemTestConstants.CONTENT
import static pl.com.betacom.itemsapp.ItemTestConstants.CREATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.ITEM_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.SHARED_OWNER_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.TITLE
import static pl.com.betacom.itemsapp.ItemTestConstants.UPDATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.UPDATED_TITLE
import static pl.com.betacom.itemsapp.ItemTestConstants.item
import static pl.com.betacom.itemsapp.ItemTestConstants.updateItemCommand
import static pl.com.betacom.itemsapp.UserTestConstants.ID

class UpdateItemServiceSpec extends Specification {

    CurrentUserProvider currentUserProvider = Mock()
    ItemRepository itemRepository = Mock()
    ItemPermissionRepository itemPermissionRepository = Mock()
    UpdateItemMapper updateItemMapper = Mock()
    UpdateItemService service = new UpdateItemService(
            currentUserProvider, itemRepository, itemPermissionRepository, updateItemMapper)

    def "saves an edit made by the owner"() {
        given:
        def current = item()
        def edited = item()

        when:
        def saved = service.update(updateItemCommand())

        then:
        1 * currentUserProvider.id() >> ID
        1 * itemRepository.findById(ITEM_ID) >> Optional.of(current)
        0 * itemPermissionRepository.findRole(_, _)
        1 * updateItemMapper.apply(current, updateItemCommand()) >> edited
        1 * itemRepository.update(edited) >> edited
        saved == edited
    }

    def "saves an edit made by an editor"() {
        given:
        def current = item()
        def edited = item()
        def command = updateItemCommand()

        when:
        def saved = service.update(command)

        then:
        1 * currentUserProvider.id() >> SHARED_OWNER_ID
        1 * itemRepository.findById(ITEM_ID) >> Optional.of(current)
        1 * itemPermissionRepository.findRole(ITEM_ID, SHARED_OWNER_ID) >> Optional.of(ItemRole.EDITOR)
        1 * updateItemMapper.apply(current, command) >> edited
        1 * itemRepository.update(edited) >> edited
        saved == edited
    }

    def "rejects a user who cannot edit"() {
        given:
        def current = item()

        when:
        service.update(updateItemCommand())

        then:
        1 * currentUserProvider.id() >> SHARED_OWNER_ID
        1 * itemRepository.findById(ITEM_ID) >> Optional.of(current)
        1 * itemPermissionRepository.findRole(ITEM_ID, SHARED_OWNER_ID) >> granted
        0 * updateItemMapper.apply(_, _)
        0 * itemRepository.update(_)
        thrown(ItemAccessDeniedException)

        where:
        granted << [Optional.of(ItemRole.VIEWER), Optional.empty()]
    }

    def "rejects a missing item"() {
        when:
        service.update(updateItemCommand())

        then:
        1 * currentUserProvider.id() >> ID
        1 * itemRepository.findById(ITEM_ID) >> Optional.empty()
        0 * itemPermissionRepository.findRole(_, _)
        thrown(ItemNotFoundException)
    }

    def "rejects a deleted item"() {
        given:
        def deleted = new Item(ITEM_ID, ID, TITLE, CONTENT, 0, true, CREATED_AT, CREATED_AT)

        when:
        service.update(updateItemCommand())

        then:
        1 * currentUserProvider.id() >> ID
        1 * itemRepository.findById(ITEM_ID) >> Optional.of(deleted)
        0 * itemPermissionRepository.findRole(_, _)
        thrown(ItemNotFoundException)
    }

    def "rejects a stale version and reports the current one"() {
        when:
        service.update(new UpdateItemCommand(ITEM_ID, UPDATED_TITLE, null, 1))

        then:
        1 * currentUserProvider.id() >> ID
        1 * itemRepository.findById(ITEM_ID) >> Optional.of(item())
        0 * itemRepository.update(_)
        def conflict = thrown(ItemVersionConflictException)
        conflict.currentVersion() == 0
    }

    def "saves a cleared content and the existing title"() {
        given:
        def clock = Mock(DateTimeProvider)
        def updating = new UpdateItemService(
                currentUserProvider, itemRepository, itemPermissionRepository, new UpdateItemMapper(clock))

        when:
        updating.update(new UpdateItemCommand(ITEM_ID, null, null, 0))

        then:
        1 * currentUserProvider.id() >> ID
        1 * itemRepository.findById(ITEM_ID) >> Optional.of(item())
        1 * clock.now() >> UPDATED_AT
        1 * itemRepository.update({ Item saved ->
            saved.title() == TITLE && saved.content() == null && saved.updatedAt() == UPDATED_AT
        }) >> { Item saved -> saved }
    }
}
