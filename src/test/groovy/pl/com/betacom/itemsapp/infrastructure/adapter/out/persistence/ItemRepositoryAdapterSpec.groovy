package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence

import org.springframework.dao.OptimisticLockingFailureException
import pl.com.betacom.itemsapp.application.port.out.GrantedItem
import pl.com.betacom.itemsapp.domain.Item
import pl.com.betacom.itemsapp.domain.ItemRole
import pl.com.betacom.itemsapp.domain.ItemVersionConflictException
import spock.lang.Specification

import static pl.com.betacom.itemsapp.ItemTestConstants.CONTENT
import static pl.com.betacom.itemsapp.ItemTestConstants.CREATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.ITEM_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.SHARED_CONTENT
import static pl.com.betacom.itemsapp.ItemTestConstants.SHARED_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.SHARED_OWNER_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.SHARED_TITLE
import static pl.com.betacom.itemsapp.ItemTestConstants.SHARED_UPDATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.UPDATED_AT
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

    def "returns the version assigned when the update is flushed"() {
        given:
        def entity = new ItemEntity(ITEM_ID, ID, TITLE, CONTENT, 0, false, CREATED_AT, CREATED_AT)
        def flushed = new ItemEntity(ITEM_ID, ID, TITLE, null, 1, false, CREATED_AT, UPDATED_AT)
        def cleared = new Item(ITEM_ID, ID, TITLE, null, 0, false, CREATED_AT, UPDATED_AT)

        when:
        def saved = adapter.update(cleared)

        then:
        1 * items.findById(ITEM_ID) >> Optional.of(entity)
        1 * items.saveAndFlush(entity) >> flushed
        entity.content == null
        saved.content() == null
        saved.title() == TITLE
        saved.version() == 1
    }

    def "marks the stored item deleted"() {
        given:
        def entity = new ItemEntity(ITEM_ID, ID, TITLE, CONTENT, 0, false, CREATED_AT, CREATED_AT)
        def deleted = new Item(ITEM_ID, ID, TITLE, CONTENT, 0, true, CREATED_AT, UPDATED_AT)

        when:
        adapter.delete(deleted)

        then:
        1 * items.findById(ITEM_ID) >> Optional.of(entity)
        0 * items.saveAndFlush(_)
        entity.deleted
        entity.updatedAt == UPDATED_AT
    }

    def "translates an optimistic lock failure into a version conflict"() {
        given:
        def entity = new ItemEntity(ITEM_ID, ID, TITLE, CONTENT, 0, false, CREATED_AT, CREATED_AT)

        when:
        adapter.update(item())

        then:
        1 * items.findById(ITEM_ID) >> Optional.of(entity)
        1 * items.saveAndFlush(entity) >> { throw new OptimisticLockingFailureException("stale") }
        def conflict = thrown(ItemVersionConflictException)
        conflict.currentVersion() == 0
    }

    def "maps an entity found by id"() {
        when:
        def found = adapter.findById(ITEM_ID)

        then:
        1 * items.findByIdAndDeleted(ITEM_ID, false) >> Optional.of(new ItemEntity(ITEM_ID, ID, TITLE, CONTENT, 0, false, CREATED_AT, CREATED_AT))
        found == Optional.of(item())
    }

    def "returns empty when no active item exists"() {
        when:
        def found = adapter.findById(ITEM_ID)

        then:
        1 * items.findByIdAndDeleted(ITEM_ID, false) >> Optional.empty()
        found == Optional.empty()
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
                SHARED_ID, SHARED_OWNER_ID, SHARED_TITLE, SHARED_CONTENT, SHARED_VERSION, false, CREATED_AT, SHARED_UPDATED_AT, ItemPermissionRole.VIEWER)

        when:
        def found = adapter.findActiveGranted(ID)

        then:
        1 * items.findActiveSharedWith(ID) >> [view]
        found == [new GrantedItem(
                new Item(SHARED_ID, SHARED_OWNER_ID, SHARED_TITLE, SHARED_CONTENT, SHARED_VERSION, false, CREATED_AT, SHARED_UPDATED_AT),
                ItemRole.VIEWER)]
    }
}
