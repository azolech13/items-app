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
import pl.com.betacom.itemsapp.application.port.in.FetchItems
import pl.com.betacom.itemsapp.infrastructure.adapter.in.security.SecurityConfig
import pl.com.betacom.itemsapp.infrastructure.adapter.in.security.SecurityFiltersConfiguration
import spock.lang.Specification

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static pl.com.betacom.itemsapp.ItemTestConstants.CONTENT
import static pl.com.betacom.itemsapp.ItemTestConstants.CREATED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.CREATE_ITEM_BODY
import static pl.com.betacom.itemsapp.ItemTestConstants.ITEM_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.TITLE
import static pl.com.betacom.itemsapp.ItemTestConstants.createItemCommand
import static pl.com.betacom.itemsapp.ItemTestConstants.item
import static pl.com.betacom.itemsapp.ItemTestConstants.fetchItem
import static pl.com.betacom.itemsapp.UserTestConstants.ID

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
