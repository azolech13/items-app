package pl.com.betacom.itemsapp.application.service

import pl.com.betacom.itemsapp.application.port.in.UpdateItemCommand
import pl.com.betacom.itemsapp.application.port.out.DateTimeProvider
import spock.lang.Specification

import static pl.com.betacom.itemsapp.ItemTestConstants.CREATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.ITEM_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.TITLE
import static pl.com.betacom.itemsapp.ItemTestConstants.UPDATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.UPDATED_CONTENT
import static pl.com.betacom.itemsapp.ItemTestConstants.UPDATED_TITLE
import static pl.com.betacom.itemsapp.ItemTestConstants.item
import static pl.com.betacom.itemsapp.ItemTestConstants.updateItemCommand
import static pl.com.betacom.itemsapp.UserTestConstants.ID

class UpdateItemMapperSpec extends Specification {

    DateTimeProvider dateTimeProvider = Mock()
    UpdateItemMapper mapper = new UpdateItemMapper(dateTimeProvider)

    def "replaces the title and clears the omitted content"() {
        when:
        def edited = mapper.apply(item(), updateItemCommand())

        then:
        1 * dateTimeProvider.now() >> UPDATED_AT
        edited.id() == ITEM_ID
        edited.ownerId() == ID
        edited.title() == UPDATED_TITLE
        edited.content() == null
        edited.version() == 0
        !edited.deleted()
        edited.createdAt() == CREATED_AT
        edited.updatedAt() == UPDATED_AT
    }

    def "replaces the content and keeps the omitted title"() {
        when:
        def edited = mapper.apply(item(), new UpdateItemCommand(ITEM_ID, null, UPDATED_CONTENT, 0))

        then:
        1 * dateTimeProvider.now() >> UPDATED_AT
        edited.title() == TITLE
        edited.content() == UPDATED_CONTENT
        edited.updatedAt() == UPDATED_AT
    }

    def "clears the content and keeps the omitted title"() {
        when:
        def edited = mapper.apply(item(), new UpdateItemCommand(ITEM_ID, null, null, 0))

        then:
        1 * dateTimeProvider.now() >> UPDATED_AT
        edited.title() == TITLE
        edited.content() == null
    }
}
