package pl.com.betacom.itemsapp.application.service

import pl.com.betacom.itemsapp.application.port.out.DateTimeProvider
import spock.lang.Specification

import static pl.com.betacom.itemsapp.ItemTestConstants.CONTENT
import static pl.com.betacom.itemsapp.ItemTestConstants.CREATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.ITEM_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.TITLE
import static pl.com.betacom.itemsapp.ItemTestConstants.UPDATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.item
import static pl.com.betacom.itemsapp.UserTestConstants.ID

class DeleteItemMapperSpec extends Specification {

    DateTimeProvider dateTimeProvider = Mock()
    DeleteItemMapper mapper = new DeleteItemMapper(dateTimeProvider)

    def "marks the item deleted and refreshes the modification time"() {
        when:
        def deleted = mapper.apply(item())

        then:
        1 * dateTimeProvider.now() >> UPDATED_AT
        deleted.id() == ITEM_ID
        deleted.ownerId() == ID
        deleted.title() == TITLE
        deleted.content() == CONTENT
        deleted.version() == 0
        deleted.deleted()
        deleted.createdAt() == CREATED_AT
        deleted.updatedAt() == UPDATED_AT
    }
}
