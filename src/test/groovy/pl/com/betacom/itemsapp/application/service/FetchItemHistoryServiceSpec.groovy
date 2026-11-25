package pl.com.betacom.itemsapp.application.service

import pl.com.betacom.itemsapp.application.port.in.ItemHistory
import pl.com.betacom.itemsapp.application.port.out.CurrentUserProvider
import pl.com.betacom.itemsapp.application.port.out.ItemHistoryRepository
import pl.com.betacom.itemsapp.application.port.out.ItemPermissionRepository
import pl.com.betacom.itemsapp.application.port.out.ItemRepository
import pl.com.betacom.itemsapp.application.port.out.ItemRevision
import pl.com.betacom.itemsapp.application.port.out.UserRepository
import pl.com.betacom.itemsapp.domain.Item
import pl.com.betacom.itemsapp.domain.ItemHistoryDeniedException
import pl.com.betacom.itemsapp.domain.ItemNotFoundException
import pl.com.betacom.itemsapp.domain.ItemRevisionType
import pl.com.betacom.itemsapp.domain.ItemRole
import pl.com.betacom.itemsapp.domain.User
import spock.lang.Specification

import static pl.com.betacom.itemsapp.ItemTestConstants.CONTENT
import static pl.com.betacom.itemsapp.ItemTestConstants.CREATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.ITEM_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.SHARED_OWNER_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.TITLE
import static pl.com.betacom.itemsapp.ItemTestConstants.UPDATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.UPDATED_CONTENT
import static pl.com.betacom.itemsapp.ItemTestConstants.UPDATED_TITLE
import static pl.com.betacom.itemsapp.ItemTestConstants.item
import static pl.com.betacom.itemsapp.UserTestConstants.ID
import static pl.com.betacom.itemsapp.UserTestConstants.LOGIN
import static pl.com.betacom.itemsapp.UserTestConstants.PASSWORD_HASH
import static pl.com.betacom.itemsapp.UserTestConstants.user
import static pl.com.betacom.itemsapp.domain.ItemHistoryDeniedException.EX_MSG

class FetchItemHistoryServiceSpec extends Specification {

    static final String OTHER_LOGIN = "anna.nowak"

    CurrentUserProvider currentUserProvider = Mock()
    ItemRepository itemRepository = Mock()
    ItemPermissionRepository itemPermissionRepository = Mock()
    ItemHistoryRepository itemHistoryRepository = Mock()
    UserRepository userRepository = Mock()
    FetchItemHistoryService service = new FetchItemHistoryService(
            currentUserProvider, itemRepository, itemPermissionRepository, itemHistoryRepository, userRepository)

    def "returns revisions for the owner and resolves logins"() {
        given:
        def added = new ItemRevision(1, ItemRevisionType.ADD, CREATED_AT, ID, TITLE, CONTENT)
        def modified = new ItemRevision(2, ItemRevisionType.MOD, UPDATED_AT, SHARED_OWNER_ID, UPDATED_TITLE, UPDATED_CONTENT)

        when:
        def history = service.fetch(ITEM_ID)

        then:
        1 * currentUserProvider.id() >> ID
        1 * itemRepository.findIncludingDeleted(ITEM_ID) >> Optional.of(item())
        0 * itemPermissionRepository.findRole(_, _)
        1 * itemHistoryRepository.findByItemId(ITEM_ID) >> [added, modified]
        1 * userRepository.findById(ID) >> user()
        1 * userRepository.findById(SHARED_OWNER_ID) >> new User(SHARED_OWNER_ID, OTHER_LOGIN, PASSWORD_HASH, CREATED_AT)
        history == [
                new ItemHistory(1, ItemRevisionType.ADD, CREATED_AT, LOGIN, TITLE, CONTENT),
                new ItemHistory(2, ItemRevisionType.MOD, UPDATED_AT, OTHER_LOGIN, UPDATED_TITLE, UPDATED_CONTENT)
        ]
    }

    def "returns revisions for a granted user"() {
        when:
        def history = service.fetch(ITEM_ID)

        then:
        1 * currentUserProvider.id() >> SHARED_OWNER_ID
        1 * itemRepository.findIncludingDeleted(ITEM_ID) >> Optional.of(item())
        1 * itemPermissionRepository.findRole(ITEM_ID, SHARED_OWNER_ID) >> Optional.of(role)
        1 * itemHistoryRepository.findByItemId(ITEM_ID) >> []
        history == []

        where:
        role << [ItemRole.EDITOR, ItemRole.VIEWER]
    }

    def "returns revisions of a deleted item"() {
        given:
        def deleted = new Item(ITEM_ID, ID, TITLE, CONTENT, 1, true, CREATED_AT, UPDATED_AT)
        def removal = new ItemRevision(2, ItemRevisionType.DEL, UPDATED_AT, ID, TITLE, CONTENT)

        when:
        def history = service.fetch(ITEM_ID)

        then:
        1 * currentUserProvider.id() >> ID
        1 * itemRepository.findIncludingDeleted(ITEM_ID) >> Optional.of(deleted)
        0 * itemPermissionRepository.findRole(_, _)
        1 * itemHistoryRepository.findByItemId(ITEM_ID) >> [removal]
        1 * userRepository.findById(ID) >> user()
        history == [new ItemHistory(2, ItemRevisionType.DEL, UPDATED_AT, LOGIN, TITLE, CONTENT)]
    }

    def "rejects a user without access"() {
        when:
        service.fetch(ITEM_ID)

        then:
        1 * currentUserProvider.id() >> SHARED_OWNER_ID
        1 * itemRepository.findIncludingDeleted(ITEM_ID) >> Optional.of(item())
        1 * itemPermissionRepository.findRole(ITEM_ID, SHARED_OWNER_ID) >> Optional.empty()
        0 * itemHistoryRepository.findByItemId(_)
        def denied = thrown(ItemHistoryDeniedException)
        denied.message == EX_MSG
    }

    def "rejects a missing item"() {
        when:
        service.fetch(ITEM_ID)

        then:
        1 * currentUserProvider.id() >> ID
        1 * itemRepository.findIncludingDeleted(ITEM_ID) >> Optional.empty()
        0 * itemPermissionRepository.findRole(_, _)
        0 * itemHistoryRepository.findByItemId(_)
        thrown(ItemNotFoundException)
    }
}
