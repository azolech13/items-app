package pl.com.betacom.itemsapp.infrastructure.adapter.in.web

import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.context.TestPropertySource
import pl.com.betacom.itemsapp.application.port.in.RegisterUser
import pl.com.betacom.itemsapp.domain.LoginTakenException
import pl.com.betacom.itemsapp.infrastructure.adapter.in.security.SecurityConfig
import pl.com.betacom.itemsapp.infrastructure.adapter.in.security.SecurityFiltersConfiguration
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static pl.com.betacom.itemsapp.UserTestConstants.CREATED_AT
import static pl.com.betacom.itemsapp.UserTestConstants.ID
import static pl.com.betacom.itemsapp.UserTestConstants.LOGIN
import static pl.com.betacom.itemsapp.UserTestConstants.REGISTER_BODY
import static pl.com.betacom.itemsapp.UserTestConstants.command
import static pl.com.betacom.itemsapp.UserTestConstants.user

@WebMvcTest(controllers = RegistrationRestController)
@Import([SecurityConfig, ApiExceptionHandler, SecurityFiltersConfiguration])
@TestPropertySource(properties = [
        "app.security.login-rate-limit.attempts=100",
        "app.security.login-rate-limit.window=1m"
])
class RegistrationRestControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @SpringBean
    RegisterUser registerUser = Mock()

    def "registers without authentication"() {
        when:
        def result = mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(REGISTER_BODY))

        then:
        1 * registerUser.register(command()) >> user()
        result.andExpect(status().isCreated())
                .andExpect(jsonPath('$.id').value(ID.toString()))
                .andExpect(jsonPath('$.login').value(LOGIN))
                .andExpect(jsonPath('$.createdAt').value(CREATED_AT.toString()))
                .andExpect(jsonPath('$.passwordHash').doesNotExist())
    }

    def "rejects a login that is too short"() {
        when:
        def result = mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"login":"ab","password":"short"}'))

        then:
        0 * registerUser.register(_)
        result.andExpect(status().isBadRequest())
    }

    def "returns conflict when the login is taken"() {
        when:
        def result = mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(REGISTER_BODY))

        then:
        1 * registerUser.register(command()) >> { throw new LoginTakenException(LOGIN) }
        result.andExpect(status().isConflict())
    }

    def "rejects anonymous access to other paths"() {
        when:
        def result = mockMvc.perform(post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))

        then:
        result.andExpect(status().isUnauthorized())
    }
}
