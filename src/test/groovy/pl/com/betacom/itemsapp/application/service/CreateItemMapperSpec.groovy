package pl.com.betacom.itemsapp.application.service

import pl.com.betacom.itemsapp.application.port.out.CurrentUserProvider
import pl.com.betacom.itemsapp.application.port.out.DateTimeProvider
import pl.com.betacom.itemsapp.application.port.out.UuidGenerator
import spock.lang.Specification

import static pl.com.betacom.itemsapp.ItemTestConstants.CONTENT
import static pl.com.betacom.itemsapp.ItemTestConstants.CREATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.ITEM_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.TITLE
import static pl.com.betacom.itemsapp.ItemTestConstants.createItemCommand
import static pl.com.betacom.itemsapp.UserTestConstants.ID

class CreateItemMapperSpec extends Specification {

    UuidGenerator uuidGenerator = Mock()
    DateTimeProvider dateTimeProvider = Mock()
    CurrentUserProvider currentUser = Mock()
    CreateItemMapper mapper = new CreateItemMapper(uuidGenerator, dateTimeProvider, currentUser)

    def "builds an item owned by the current user"() {
        when:
        def item = mapper.toItem(createItemCommand())

        then:
        1 * uuidGenerator.generate() >> ITEM_ID
        1 * currentUser.id() >> ID
        1 * dateTimeProvider.now() >> CREATED_AT
        item.id() == ITEM_ID
        item.ownerId() == ID
        item.title() == TITLE
        item.content() == CONTENT
        item.version() == null
        !item.deleted()
        item.createdAt() == CREATED_AT
        item.updatedAt() == CREATED_AT
    }
}
