package pl.com.betacom.itemsapp.infrastructure.adapter.in.web

import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import pl.com.betacom.itemsapp.application.port.in.CreateItem
import pl.com.betacom.itemsapp.application.port.in.DeleteItem
import pl.com.betacom.itemsapp.application.port.in.FetchItemHistory
import pl.com.betacom.itemsapp.application.port.in.FetchItems
import pl.com.betacom.itemsapp.application.port.in.ItemHistory
import pl.com.betacom.itemsapp.application.port.in.UpdateItem
import pl.com.betacom.itemsapp.application.port.in.UpdateItemCommand
import pl.com.betacom.itemsapp.domain.Item
import pl.com.betacom.itemsapp.domain.ItemDeleteDeniedException
import pl.com.betacom.itemsapp.domain.ItemEditDeniedException
import pl.com.betacom.itemsapp.domain.ItemHistoryDeniedException
import pl.com.betacom.itemsapp.domain.ItemNotFoundException
import pl.com.betacom.itemsapp.domain.ItemRevisionType
import pl.com.betacom.itemsapp.domain.ItemVersionConflictException
import pl.com.betacom.itemsapp.infrastructure.adapter.in.security.SecurityConfig
import pl.com.betacom.itemsapp.infrastructure.adapter.in.security.SecurityFiltersConfiguration
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.hamcrest.Matchers.nullValue
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static pl.com.betacom.itemsapp.ItemTestConstants.CONTENT
import static pl.com.betacom.itemsapp.ItemTestConstants.CREATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.CREATE_ITEM_BODY
import static pl.com.betacom.itemsapp.ItemTestConstants.ITEM_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.TITLE
import static pl.com.betacom.itemsapp.ItemTestConstants.UPDATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.UPDATED_CONTENT
import static pl.com.betacom.itemsapp.ItemTestConstants.UPDATED_TITLE
import static pl.com.betacom.itemsapp.ItemTestConstants.UPDATE_ITEM_BODY
import static pl.com.betacom.itemsapp.ItemTestConstants.createItemCommand
import static pl.com.betacom.itemsapp.ItemTestConstants.fetchItem
import static pl.com.betacom.itemsapp.ItemTestConstants.item
import static pl.com.betacom.itemsapp.ItemTestConstants.updateItemCommand
import static pl.com.betacom.itemsapp.UserTestConstants.ID
import static pl.com.betacom.itemsapp.UserTestConstants.LOGIN

@WebMvcTest(controllers = ItemRestController)
@Import([SecurityConfig, ApiExceptionHandler, SecurityFiltersConfiguration])
@TestPropertySource(properties = [
        "app.security.login-rate-limit.attempts=100",
        "app.security.login-rate-limit.window=1m"
])
class ItemRestControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @SpringBean
    CreateItem createItem = Mock()

    @SpringBean
    FetchItems listItems = Mock()

    @SpringBean
    FetchItemHistory fetchItemHistory = Mock()

    @SpringBean
    UpdateItem updateItem = Mock()

    @SpringBean
    DeleteItem deleteItem = Mock()

    def "creates an item for the authenticated user"() {
        when:
        def result = mockMvc.perform(post("/items")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE_ITEM_BODY))

        then:
        1 * createItem.create(createItemCommand()) >> item()
        result.andExpect(status().isCreated())
                .andExpect(jsonPath('$.id').value(ITEM_ID.toString()))
                .andExpect(jsonPath('$.title').value(TITLE))
                .andExpect(jsonPath('$.content').value(CONTENT))
                .andExpect(jsonPath('$.version').value(0))
                .andExpect(jsonPath('$.ownerId').value(ID.toString()))
                .andExpect(jsonPath('$.createdAt').value(CREATED_AT.toString()))
                .andExpect(jsonPath('$.updatedAt').value(CREATED_AT.toString()))
                .andExpect(jsonPath('$.deleted').doesNotExist())
    }

    def "rejects a missing title"() {
        when:
        def result = mockMvc.perform(post("/items")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of())))
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"content":"Treść notatki..."}'))

        then:
        0 * createItem.create(_)
        result.andExpect(status().isBadRequest())
    }

    def "updates an item for the authenticated user"() {
        given:
        def edited = new Item(ITEM_ID, ID, UPDATED_TITLE, CONTENT, 1, false, CREATED_AT, UPDATED_AT)

        when:
        def result = mockMvc.perform(patch("/items/$ITEM_ID")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(UPDATE_ITEM_BODY))

        then:
        1 * updateItem.update(updateItemCommand()) >> edited
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.id').value(ITEM_ID.toString()))
                .andExpect(jsonPath('$.title').value(UPDATED_TITLE))
                .andExpect(jsonPath('$.content').value(CONTENT))
                .andExpect(jsonPath('$.version').value(1))
                .andExpect(jsonPath('$.updatedAt').value(UPDATED_AT.toString()))
                .andExpect(jsonPath('$.createdAt').doesNotExist())
                .andExpect(jsonPath('$.ownerId').doesNotExist())
    }

    def "rejects an update without a version"() {
        when:
        def result = mockMvc.perform(patch("/items/$ITEM_ID")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of())))
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"title":"' + UPDATED_TITLE + '"}'))

        then:
        0 * updateItem.update(_)
        result.andExpect(status().isBadRequest())
    }

    def "clears the content when the title is omitted"() {
        given:
        def cleared = new Item(ITEM_ID, ID, TITLE, null, 1, false, CREATED_AT, UPDATED_AT)

        when:
        def result = mockMvc.perform(patch("/items/$ITEM_ID")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of())))
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"content":null,"version":0}'))

        then:
        1 * updateItem.update(new UpdateItemCommand(ITEM_ID, null, null, 0)) >> cleared
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.title').value(TITLE))
                .andExpect(jsonPath('$.content').value(nullValue()))
    }

    def "keeps a null title and replaces the content"() {
        given:
        def edited = new Item(ITEM_ID, ID, TITLE, UPDATED_CONTENT, 1, false, CREATED_AT, UPDATED_AT)

        when:
        def result = mockMvc.perform(patch("/items/$ITEM_ID")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of())))
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"title":null,"content":"' + UPDATED_CONTENT + '","version":0}'))

        then:
        1 * updateItem.update(new UpdateItemCommand(ITEM_ID, null, UPDATED_CONTENT, 0)) >> edited
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.title').value(TITLE))
                .andExpect(jsonPath('$.content').value(UPDATED_CONTENT))
    }

    def "rejects a blank title"() {
        when:
        def result = mockMvc.perform(patch("/items/$ITEM_ID")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of())))
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"title":"","version":0}'))

        then:
        0 * updateItem.update(_)
        result.andExpect(status().isBadRequest())
    }

    def "clears the content when title and content are null"() {
        given:
        def cleared = new Item(ITEM_ID, ID, TITLE, null, 1, false, CREATED_AT, UPDATED_AT)

        when:
        def result = mockMvc.perform(patch("/items/$ITEM_ID")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of())))
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"version":0}'))

        then:
        1 * updateItem.update(new UpdateItemCommand(ITEM_ID, null, null, 0)) >> cleared
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.title').value(TITLE))
                .andExpect(jsonPath('$.content').value(nullValue()))
    }

    def "rejects anonymous update"() {
        when:
        def result = mockMvc.perform(patch("/items/$ITEM_ID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(UPDATE_ITEM_BODY))

        then:
        0 * updateItem.update(_)
        result.andExpect(status().isUnauthorized())
    }

    def "rejects an update without permission"() {
        when:
        def result = mockMvc.perform(patch("/items/$ITEM_ID")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(UPDATE_ITEM_BODY))

        then:
        1 * updateItem.update(updateItemCommand()) >> { throw new ItemEditDeniedException() }
        result.andExpect(status().isForbidden())
    }

    def "rejects an update of a missing item"() {
        when:
        def result = mockMvc.perform(patch("/items/$ITEM_ID")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(UPDATE_ITEM_BODY))

        then:
        1 * updateItem.update(updateItemCommand()) >> { throw new ItemNotFoundException(ITEM_ID) }
        result.andExpect(status().isNotFound())
    }

    def "rejects a stale version and returns the current one"() {
        when:
        def result = mockMvc.perform(patch("/items/$ITEM_ID")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(UPDATE_ITEM_BODY))

        then:
        1 * updateItem.update(updateItemCommand()) >> { throw new ItemVersionConflictException(2) }
        result.andExpect(status().isConflict())
                .andExpect(jsonPath('$.detail').value(String.format(ItemVersionConflictException.EX_MSG, 2)))
    }

    def "deletes an item for the owner"() {
        when:
        def result = mockMvc.perform(delete("/items/$ITEM_ID")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of()))))

        then:
        1 * deleteItem.delete(ITEM_ID)
        result.andExpect(status().isNoContent())
    }

    def "rejects anonymous delete"() {
        when:
        def result = mockMvc.perform(delete("/items/$ITEM_ID"))

        then:
        0 * deleteItem.delete(_)
        result.andExpect(status().isUnauthorized())
    }

    def "rejects a delete without ownership"() {
        when:
        def result = mockMvc.perform(delete("/items/$ITEM_ID")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of()))))

        then:
        1 * deleteItem.delete(ITEM_ID) >> { throw new ItemDeleteDeniedException() }
        result.andExpect(status().isForbidden())
    }

    def "rejects a delete of a missing item"() {
        when:
        def result = mockMvc.perform(delete("/items/$ITEM_ID")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of()))))

        then:
        1 * deleteItem.delete(ITEM_ID) >> { throw new ItemNotFoundException(ITEM_ID) }
        result.andExpect(status().isNotFound())
    }

    def "returns the item history"() {
        given:
        def history = [
                new ItemHistory(1, ItemRevisionType.ADD, CREATED_AT, LOGIN, TITLE, CONTENT),
                new ItemHistory(2, ItemRevisionType.MOD, UPDATED_AT, "anna.nowak", UPDATED_TITLE, UPDATED_CONTENT)
        ]

        when:
        def result = mockMvc.perform(get("/items/$ITEM_ID/history")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of()))))

        then:
        1 * fetchItemHistory.fetch(ITEM_ID) >> history
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$[0].revision').value(1))
                .andExpect(jsonPath('$[0].revisionType').value("ADD"))
                .andExpect(jsonPath('$[0].timestamp').value(CREATED_AT.toString()))
                .andExpect(jsonPath('$[0].changedBy').value(LOGIN))
                .andExpect(jsonPath('$[0].title').value(TITLE))
                .andExpect(jsonPath('$[0].content').value(CONTENT))
                .andExpect(jsonPath('$[1].revision').value(2))
                .andExpect(jsonPath('$[1].revisionType').value("MOD"))
                .andExpect(jsonPath('$[1].changedBy').value("anna.nowak"))
                .andExpect(jsonPath('$[1].title').value(UPDATED_TITLE))
                .andExpect(jsonPath('$[1].content').value(UPDATED_CONTENT))
    }

    def "returns an empty history"() {
        when:
        def result = mockMvc.perform(get("/items/$ITEM_ID/history")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of()))))

        then:
        1 * fetchItemHistory.fetch(ITEM_ID) >> []
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.length()').value(0))
    }

    def "rejects anonymous history"() {
        when:
        def result = mockMvc.perform(get("/items/$ITEM_ID/history"))

        then:
        0 * fetchItemHistory.fetch(_)
        result.andExpect(status().isUnauthorized())
    }

    def "rejects history without access"() {
        when:
        def result = mockMvc.perform(get("/items/$ITEM_ID/history")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of()))))

        then:
        1 * fetchItemHistory.fetch(ITEM_ID) >> { throw new ItemHistoryDeniedException() }
        result.andExpect(status().isForbidden())
    }

    def "rejects history of a missing item"() {
        when:
        def result = mockMvc.perform(get("/items/$ITEM_ID/history")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of()))))

        then:
        1 * fetchItemHistory.fetch(ITEM_ID) >> { throw new ItemNotFoundException(ITEM_ID) }
        result.andExpect(status().isNotFound())
    }

    def "lists items for the authenticated user"() {
        when:
        def result = mockMvc.perform(get("/items")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of()))))

        then:
        1 * listItems.fetch() >> [fetchItem()]
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$[0].id').value(ITEM_ID.toString()))
                .andExpect(jsonPath('$[0].title').value(TITLE))
                .andExpect(jsonPath('$[0].content').value(CONTENT))
                .andExpect(jsonPath('$[0].version').value(0))
                .andExpect(jsonPath('$[0].ownerId').value(ID.toString()))
                .andExpect(jsonPath('$[0].myRole').value('OWNER'))
                .andExpect(jsonPath('$[0].updatedAt').value(CREATED_AT.toString()))
                .andExpect(jsonPath('$[0].createdAt').doesNotExist())
                .andExpect(jsonPath('$[0].deleted').doesNotExist())
    }

    def "returns an empty list"() {
        when:
        def result = mockMvc.perform(get("/items")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of()))))

        then:
        1 * listItems.fetch() >> []
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$').isEmpty())
    }

    def "rejects anonymous listing"() {
        when:
        def result = mockMvc.perform(get("/items"))

        then:
        0 * listItems.fetch()
        result.andExpect(status().isUnauthorized())
    }
}
