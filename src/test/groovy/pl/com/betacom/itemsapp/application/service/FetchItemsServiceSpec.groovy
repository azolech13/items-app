package pl.com.betacom.itemsapp.application.service

import pl.com.betacom.itemsapp.application.port.in.FetchItem
import pl.com.betacom.itemsapp.application.port.out.CurrentUserProvider
import pl.com.betacom.itemsapp.application.port.out.GrantedItem
import pl.com.betacom.itemsapp.application.port.out.ItemRepository
import pl.com.betacom.itemsapp.domain.Item
import pl.com.betacom.itemsapp.domain.ItemRole
import spock.lang.Specification

import static pl.com.betacom.itemsapp.ItemTestConstants.CONTENT
import static pl.com.betacom.itemsapp.ItemTestConstants.CREATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.ITEM_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.SHARED_CONTENT
import static pl.com.betacom.itemsapp.ItemTestConstants.SHARED_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.SHARED_OWNER_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.SHARED_TITLE
import static pl.com.betacom.itemsapp.ItemTestConstants.SHARED_UPDATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.SHARED_VERSION
import static pl.com.betacom.itemsapp.ItemTestConstants.TITLE
import static pl.com.betacom.itemsapp.ItemTestConstants.item
import static pl.com.betacom.itemsapp.UserTestConstants.ID

class FetchItemsServiceSpec extends Specification {

    CurrentUserProvider currentUserProvider = Mock()
    ItemRepository itemRepository = Mock()
    FetchItemsService service = new FetchItemsService(currentUserProvider, itemRepository)

    def "lists an owned item and a shared item newest first"() {
        given:
        def owned = item()
        def shared = new Item(SHARED_ID, SHARED_OWNER_ID, SHARED_TITLE, SHARED_CONTENT, SHARED_VERSION, false, CREATED_AT, SHARED_UPDATED_AT)

        when:
        def listed = service.fetch()

        then:
        1 * currentUserProvider.id() >> ID
        1 * itemRepository.findActiveByOwner(ID) >> [owned]
        1 * itemRepository.findActiveGranted(ID) >> [new GrantedItem(shared, ItemRole.VIEWER)]
        listed == [
                new FetchItem(SHARED_ID, SHARED_TITLE, SHARED_CONTENT, SHARED_VERSION, SHARED_OWNER_ID, ItemRole.VIEWER, SHARED_UPDATED_AT),
                new FetchItem(ITEM_ID, TITLE, CONTENT, 0, ID, ItemRole.OWNER, CREATED_AT)
        ]
    }
}
