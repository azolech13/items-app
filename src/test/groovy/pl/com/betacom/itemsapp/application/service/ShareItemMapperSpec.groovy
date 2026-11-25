package pl.com.betacom.itemsapp.application.service

import pl.com.betacom.itemsapp.application.port.in.ShareItemCommand
import pl.com.betacom.itemsapp.application.port.in.ShareResult
import pl.com.betacom.itemsapp.application.port.out.DateTimeProvider
import pl.com.betacom.itemsapp.application.port.out.UuidGenerator
import pl.com.betacom.itemsapp.domain.ItemPermission
import pl.com.betacom.itemsapp.domain.ItemRole
import spock.lang.Specification

import static pl.com.betacom.itemsapp.ItemTestConstants.CREATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.GRANTED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.ITEM_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.PERMISSION_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.SHARED_OWNER_ID

class ShareItemMapperSpec extends Specification {

    UuidGenerator uuidGenerator = Mock()
    DateTimeProvider dateTimeProvider = Mock()
    ShareItemMapper mapper = new ShareItemMapper(uuidGenerator, dateTimeProvider)

    def "fills both timestamps when the grant is new"() {
        when:
        def permission = mapper.toNew(new ShareItemCommand(ITEM_ID, SHARED_OWNER_ID, ItemRole.EDITOR))

        then:
        1 * uuidGenerator.generate() >> PERMISSION_ID
        1 * dateTimeProvider.now() >> GRANTED_AT
        permission.id() == PERMISSION_ID
        permission.itemId() == ITEM_ID
        permission.userId() == SHARED_OWNER_ID
        permission.role() == ItemRole.EDITOR
        permission.createdAt() == GRANTED_AT
        permission.updatedAt() == GRANTED_AT
    }

    def "keeps the original creation time when the role changes"() {
        given:
        def existing = new ItemPermission(PERMISSION_ID, ITEM_ID, SHARED_OWNER_ID, ItemRole.VIEWER, CREATED_AT, CREATED_AT)

        when:
        def permission = mapper.toUpdated(existing, ItemRole.EDITOR)

        then:
        1 * dateTimeProvider.now() >> GRANTED_AT
        permission.id() == PERMISSION_ID
        permission.role() == ItemRole.EDITOR
        permission.createdAt() == CREATED_AT
        permission.updatedAt() == GRANTED_AT
    }

    def "takes grantedAt from the last change"() {
        given:
        def permission = new ItemPermission(PERMISSION_ID, ITEM_ID, SHARED_OWNER_ID, ItemRole.EDITOR, CREATED_AT, GRANTED_AT)

        when:
        def result = mapper.toResult(permission, created)

        then:
        result == new ShareResult(ITEM_ID, SHARED_OWNER_ID, ItemRole.EDITOR, GRANTED_AT, created)

        where:
        created << [true, false]
    }
}
