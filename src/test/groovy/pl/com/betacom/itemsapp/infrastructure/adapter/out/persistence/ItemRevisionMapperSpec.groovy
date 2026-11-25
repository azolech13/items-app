package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence

import org.hibernate.envers.RevisionType
import pl.com.betacom.itemsapp.domain.ItemRevisionType
import spock.lang.Specification

import static pl.com.betacom.itemsapp.ItemTestConstants.CONTENT
import static pl.com.betacom.itemsapp.ItemTestConstants.CREATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.ITEM_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.TITLE
import static pl.com.betacom.itemsapp.ItemTestConstants.UPDATED_AT
import static pl.com.betacom.itemsapp.UserTestConstants.ID

class ItemRevisionMapperSpec extends Specification {

    def "maps an envers revision type"() {
        expect:
        ItemRevisionMapper.toType(envers, deleted) == api

        where:
        envers           | deleted | api
        RevisionType.ADD | false   | ItemRevisionType.ADD
        RevisionType.ADD | true    | ItemRevisionType.ADD
        RevisionType.MOD | false   | ItemRevisionType.MOD
        RevisionType.MOD | true    | ItemRevisionType.DEL
        RevisionType.DEL | false   | ItemRevisionType.DEL
        RevisionType.DEL | true    | ItemRevisionType.DEL
    }

    def "maps a revision row"() {
        given:
        def entity = new ItemEntity(ITEM_ID, ID, TITLE, CONTENT, 0, true, CREATED_AT, UPDATED_AT)
        def stored = new AuthorRevision()
        stored.id = 3
        stored.timestamp = CREATED_AT.toEpochMilli()
        stored.changedBy = ID.toString()

        when:
        def revision = ItemRevisionMapper.toRevision([entity, stored, RevisionType.MOD] as Object[])

        then:
        revision.revision() == 3
        revision.revisionType() == ItemRevisionType.DEL
        revision.timestamp() == CREATED_AT
        revision.changedBy() == ID
        revision.title() == TITLE
        revision.content() == CONTENT
    }
}
