package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence

import pl.com.betacom.itemsapp.domain.ItemPermission
import pl.com.betacom.itemsapp.domain.ItemRole
import spock.lang.Specification

import static pl.com.betacom.itemsapp.ItemTestConstants.CREATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.GRANTED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.ITEM_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.PERMISSION_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.SHARED_OWNER_ID
import static pl.com.betacom.itemsapp.UserTestConstants.ID

class ItemPermissionRepositoryAdapterSpec extends Specification {

    ItemPermissionJpaRepository permissions = Mock()
    ItemPermissionRepositoryAdapter adapter = new ItemPermissionRepositoryAdapter(permissions)

    def "maps the stored role to the domain"() {
        when:
        def role = adapter.findRole(ITEM_ID, ID)

        then:
        1 * permissions.findRoleByItemIdAndUserId(ITEM_ID, ID) >> Optional.of(ItemPermissionRole.EDITOR)
        role == Optional.of(ItemRole.EDITOR)
    }

    def "inserts a new permission"() {
        given:
        def permission = new ItemPermission(PERMISSION_ID, ITEM_ID, SHARED_OWNER_ID, ItemRole.EDITOR, GRANTED_AT, GRANTED_AT)
        def entity = new ItemPermissionEntity(PERMISSION_ID, ITEM_ID, SHARED_OWNER_ID, ItemPermissionRole.EDITOR, GRANTED_AT, GRANTED_AT)

        when:
        def saved = adapter.insert(permission)

        then:
        1 * permissions.saveAndFlush({ ItemPermissionEntity stored ->
            stored.id == PERMISSION_ID && stored.role == ItemPermissionRole.EDITOR
                    && stored.createdAt == GRANTED_AT && stored.updatedAt == GRANTED_AT
        }) >> entity
        saved == permission
    }

    def "updates the role and the modification time"() {
        given:
        def stored = new ItemPermissionEntity(PERMISSION_ID, ITEM_ID, SHARED_OWNER_ID, ItemPermissionRole.VIEWER, CREATED_AT, CREATED_AT)
        def permission = new ItemPermission(PERMISSION_ID, ITEM_ID, SHARED_OWNER_ID, ItemRole.EDITOR, CREATED_AT, GRANTED_AT)

        when:
        def saved = adapter.update(permission)

        then:
        1 * permissions.findByItemIdAndUserId(ITEM_ID, SHARED_OWNER_ID) >> Optional.of(stored)
        1 * permissions.saveAndFlush(stored) >> stored
        stored.role == ItemPermissionRole.EDITOR
        stored.updatedAt == GRANTED_AT
        stored.createdAt == CREATED_AT
        saved.role() == ItemRole.EDITOR
        saved.updatedAt() == GRANTED_AT
    }
}
