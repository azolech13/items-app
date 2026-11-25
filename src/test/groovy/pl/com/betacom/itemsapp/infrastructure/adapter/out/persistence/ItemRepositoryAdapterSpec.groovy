package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence

import pl.com.betacom.itemsapp.application.port.out.GrantedItem
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

class ItemRepositoryAdapterSpec extends Specification {

    ItemJpaRepository items = Mock()
    ItemRepositoryAdapter adapter = new ItemRepositoryAdapter(items)

    def "returns the version assigned when the insert is flushed"() {
        given:
        def draft = new Item(ITEM_ID, ID, TITLE, CONTENT, null, false, CREATED_AT, CREATED_AT)
        def persisted = new ItemEntity(ITEM_ID, ID, TITLE, CONTENT, 0, false, CREATED_AT, CREATED_AT)

        when:
        def saved = adapter.save(draft)

        then:
        1 * items.saveAndFlush({ ItemEntity entity ->
            entity.id == ITEM_ID && entity.version == null && entity.ownerId == ID
        }) >> persisted
        saved.version() == 0
        saved.id() == ITEM_ID
        saved.ownerId() == ID
    }

    def "maps owned entities to items"() {
        when:
        def found = adapter.findActiveByOwner(ID)

        then:
        1 * items.findActiveByOwner(ID) >> [new ItemEntity(ITEM_ID, ID, TITLE, CONTENT, 0, false, CREATED_AT, CREATED_AT)]
        found == [item()]
    }

    def "maps a granted view to an item with its role"() {
        given:
        def view = new GrantedItemView(
                SHARED_ID, SHARED_OWNER_ID, SHARED_TITLE, SHARED_CONTENT, SHARED_VERSION, false, CREATED_AT, SHARED_UPDATED_AT, ItemRole.VIEWER)

        when:
        def found = adapter.findActiveGranted(ID)

        then:
        1 * items.findActiveSharedWith(ID) >> [view]
        found == [new GrantedItem(
                new Item(SHARED_ID, SHARED_OWNER_ID, SHARED_TITLE, SHARED_CONTENT, SHARED_VERSION, false, CREATED_AT, SHARED_UPDATED_AT),
                ItemRole.VIEWER)]
    }
}
