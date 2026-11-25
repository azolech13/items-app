package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence

import pl.com.betacom.itemsapp.domain.Item
import spock.lang.Specification

import static pl.com.betacom.itemsapp.ItemTestConstants.CONTENT
import static pl.com.betacom.itemsapp.ItemTestConstants.CREATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.ITEM_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.TITLE
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
}
