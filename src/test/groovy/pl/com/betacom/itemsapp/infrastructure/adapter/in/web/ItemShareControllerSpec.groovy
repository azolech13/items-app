package pl.com.betacom.itemsapp.infrastructure.adapter.in.web

import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import pl.com.betacom.itemsapp.application.port.in.DeleteItemShare
import pl.com.betacom.itemsapp.application.port.in.DeleteItemShareCommand
import pl.com.betacom.itemsapp.application.port.in.ShareItem
import pl.com.betacom.itemsapp.application.port.in.ShareItemCommand
import pl.com.betacom.itemsapp.application.port.in.ShareResult
import pl.com.betacom.itemsapp.domain.ItemNotFoundException
import pl.com.betacom.itemsapp.domain.ItemPermissionNotFoundException
import pl.com.betacom.itemsapp.domain.ItemRevokeDeniedException
import pl.com.betacom.itemsapp.domain.ItemRole
import pl.com.betacom.itemsapp.domain.ItemShareDeniedException
import pl.com.betacom.itemsapp.domain.SelfShareException
import pl.com.betacom.itemsapp.domain.UserNotFoundException
import pl.com.betacom.itemsapp.infrastructure.adapter.in.security.SecurityConfig
import pl.com.betacom.itemsapp.infrastructure.adapter.in.security.SecurityFiltersConfiguration
import spock.lang.Specification

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static pl.com.betacom.itemsapp.ItemTestConstants.GRANTED_AT
import static pl.com.betacom.itemsapp.ItemTestConstants.ITEM_ID
import static pl.com.betacom.itemsapp.ItemTestConstants.SHARED_OWNER_ID
import static pl.com.betacom.itemsapp.UserTestConstants.ID

@WebMvcTest(controllers = ItemShareController)
@Import([SecurityConfig, ApiExceptionHandler, SecurityFiltersConfiguration])
@TestPropertySource(properties = [
        "app.security.login-rate-limit.attempts=100",
        "app.security.login-rate-limit.window=1m"
])
class ItemShareControllerSpec extends Specification {

    static final String BODY = '{"userId":"' + SHARED_OWNER_ID + '","role":"EDITOR"}'

    @Autowired
    MockMvc mockMvc

    @SpringBean
    ShareItem shareItem = Mock()

    @SpringBean
    DeleteItemShare deleteItemShare = Mock()

    def "creates a grant"() {
        when:
        def result = mockMvc.perform(post("/items/$ITEM_ID/share")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))

        then:
        1 * shareItem.share(new ShareItemCommand(ITEM_ID, SHARED_OWNER_ID, ItemRole.EDITOR)) >> granted(true)
        result.andExpect(status().isCreated())
                .andExpect(jsonPath('$.itemId').value(ITEM_ID.toString()))
                .andExpect(jsonPath('$.userId').value(SHARED_OWNER_ID.toString()))
                .andExpect(jsonPath('$.role').value("EDITOR"))
                .andExpect(jsonPath('$.grantedAt').value(GRANTED_AT.toString()))
    }

    def "updates an existing grant"() {
        when:
        def result = mockMvc.perform(post("/items/$ITEM_ID/share")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))

        then:
        1 * shareItem.share(new ShareItemCommand(ITEM_ID, SHARED_OWNER_ID, ItemRole.EDITOR)) >> granted(false)
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.grantedAt').value(GRANTED_AT.toString()))
    }

    def "rejects a missing role"() {
        when:
        def result = mockMvc.perform(post("/items/$ITEM_ID/share")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of())))
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"userId":"' + SHARED_OWNER_ID + '"}'))

        then:
        0 * shareItem.share(_)
        result.andExpect(status().isBadRequest())
    }

    def "rejects an invalid role"() {
        when:
        def result = mockMvc.perform(post("/items/$ITEM_ID/share")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of())))
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"userId":"' + SHARED_OWNER_ID + '","role":"OWNER"}'))

        then:
        0 * shareItem.share(_)
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.errors[0]').value("role: accepts only VIEWER, EDITOR"))
    }

    def "rejects granting a role to the owner"() {
        when:
        def result = mockMvc.perform(post("/items/$ITEM_ID/share")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))

        then:
        1 * shareItem.share(_) >> { throw new SelfShareException() }
        result.andExpect(status().isBadRequest())
    }

    def "rejects anonymous share"() {
        when:
        def result = mockMvc.perform(post("/items/$ITEM_ID/share")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))

        then:
        0 * shareItem.share(_)
        result.andExpect(status().isUnauthorized())
    }

    def "rejects a user who is not the owner"() {
        when:
        def result = mockMvc.perform(post("/items/$ITEM_ID/share")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))

        then:
        1 * shareItem.share(_) >> { throw new ItemShareDeniedException() }
        result.andExpect(status().isForbidden())
    }

    def "rejects a missing item"() {
        when:
        def result = mockMvc.perform(post("/items/$ITEM_ID/share")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))

        then:
        1 * shareItem.share(_) >> { throw new ItemNotFoundException(ITEM_ID) }
        result.andExpect(status().isNotFound())
    }

    def "rejects a missing user"() {
        when:
        def result = mockMvc.perform(post("/items/$ITEM_ID/share")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))

        then:
        1 * shareItem.share(_) >> { throw new UserNotFoundException(SHARED_OWNER_ID) }
        result.andExpect(status().isNotFound())
    }

    def "revokes a grant"() {
        when:
        def result = mockMvc.perform(delete("/items/$ITEM_ID/share/$SHARED_OWNER_ID")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of()))))

        then:
        1 * deleteItemShare.delete(new DeleteItemShareCommand(ITEM_ID, SHARED_OWNER_ID))
        result.andExpect(status().isNoContent())
    }

    def "rejects anonymous revoke"() {
        when:
        def result = mockMvc.perform(delete("/items/$ITEM_ID/share/$SHARED_OWNER_ID"))

        then:
        0 * deleteItemShare.delete(_)
        result.andExpect(status().isUnauthorized())
    }

    def "rejects a revoke from a user who is not the owner"() {
        when:
        def result = mockMvc.perform(delete("/items/$ITEM_ID/share/$SHARED_OWNER_ID")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of()))))

        then:
        1 * deleteItemShare.delete(_) >> { throw new ItemRevokeDeniedException() }
        result.andExpect(status().isForbidden())
    }

    def "rejects a revoke of a missing item"() {
        when:
        def result = mockMvc.perform(delete("/items/$ITEM_ID/share/$SHARED_OWNER_ID")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of()))))

        then:
        1 * deleteItemShare.delete(_) >> { throw new ItemNotFoundException(ITEM_ID) }
        result.andExpect(status().isNotFound())
    }

    def "rejects a revoke when the user has no grant"() {
        when:
        def result = mockMvc.perform(delete("/items/$ITEM_ID/share/$SHARED_OWNER_ID")
                .with(authentication(new UsernamePasswordAuthenticationToken(ID, null, List.of()))))

        then:
        1 * deleteItemShare.delete(_) >> { throw new ItemPermissionNotFoundException(ITEM_ID, SHARED_OWNER_ID) }
        result.andExpect(status().isNotFound())
    }

    private static ShareResult granted(boolean created) {
        new ShareResult(ITEM_ID, SHARED_OWNER_ID, ItemRole.EDITOR, GRANTED_AT, created)
    }
}
