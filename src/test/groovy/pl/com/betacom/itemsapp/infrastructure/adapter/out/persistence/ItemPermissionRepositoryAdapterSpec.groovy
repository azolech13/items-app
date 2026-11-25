package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence

import pl.com.betacom.itemsapp.domain.ItemRole
import spock.lang.Specification

import static pl.com.betacom.itemsapp.ItemTestConstants.ITEM_ID
import static pl.com.betacom.itemsapp.UserTestConstants.ID

class ItemPermissionRepositoryAdapterSpec extends Specification {

    ItemPermissionJpaRepository permissions = Mock()
    ItemPermissionRepositoryAdapter adapter = new ItemPermissionRepositoryAdapter(permissions)

    def "maps the stored role to the domain"() {
        when:
        def role = adapter.findRole(ITEM_ID, ID)

        then:
        1 * permissions.findByItemIdAndUserId(ITEM_ID, ID) >> Optional.of(ItemPermissionRole.EDITOR)
        role == Optional.of(ItemRole.EDITOR)
    }
}
