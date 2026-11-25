package pl.com.betacom.itemsapp.application.service

import pl.com.betacom.itemsapp.application.port.out.CurrentUserProvider
import pl.com.betacom.itemsapp.application.port.out.ItemRepository
import pl.com.betacom.itemsapp.domain.Item
import pl.com.betacom.itemsapp.domain.ItemDeleteDeniedException
import pl.com.betacom.itemsapp.domain.ItemNotFoundException
import spock.lang.Specification

import static pl.com.betacom.itemsapp.ItemTestConstants.CONTENT
import static pl.com.betacom.itemsapp.ItemTestConstants.CREATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.ITEM_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.SHARED_OWNER_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.TITLE
import static pl.com.betacom.itemsapp.ItemTestConstants.UPDATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.item
import static pl.com.betacom.itemsapp.UserTestConstants.ID

class DeleteItemServiceSpec extends Specification {

    CurrentUserProvider currentUserProvider = Mock()
    ItemRepository itemRepository = Mock()
    DeleteItemMapper deleteItemMapper = Mock()
    DeleteItemService service = new DeleteItemService(currentUserProvider, itemRepository, deleteItemMapper)

    def "deletes an item owned by the current user"() {
        given:
        def current = item()
        def deleted = new Item(ITEM_ID, ID, TITLE, CONTENT, 0, true, CREATED_AT, UPDATED_AT)

        when:
        service.delete(ITEM_ID)

        then:
        1 * currentUserProvider.id() >> ID
        1 * itemRepository.findById(ITEM_ID) >> Optional.of(current)
        1 * deleteItemMapper.apply(current) >> deleted
        1 * itemRepository.delete(deleted)
    }

    def "rejects a user who is not the owner"() {
        when:
        service.delete(ITEM_ID)

        then:
        1 * currentUserProvider.id() >> SHARED_OWNER_ID
        1 * itemRepository.findById(ITEM_ID) >> Optional.of(item())
        0 * deleteItemMapper.apply(_)
        0 * itemRepository.delete(_)
        def denied = thrown(ItemDeleteDeniedException)
        denied.message == ItemDeleteDeniedException.EX_MSG
    }

    def "rejects a missing item"() {
        when:
        service.delete(ITEM_ID)

        then:
        1 * currentUserProvider.id() >> ID
        1 * itemRepository.findById(ITEM_ID) >> Optional.empty()
        0 * itemRepository.delete(_)
        thrown(ItemNotFoundException)
    }

}
