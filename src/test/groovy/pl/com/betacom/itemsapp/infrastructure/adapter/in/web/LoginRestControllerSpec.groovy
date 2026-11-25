package pl.com.betacom.itemsapp.infrastructure.adapter.in.web

import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import pl.com.betacom.itemsapp.application.port.in.LoginResult
import pl.com.betacom.itemsapp.application.port.in.LoginUser
import pl.com.betacom.itemsapp.domain.InvalidCredentialsException
import pl.com.betacom.itemsapp.infrastructure.adapter.in.security.SecurityConfig
import pl.com.betacom.itemsapp.infrastructure.adapter.in.security.SecurityFiltersConfiguration
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static pl.com.betacom.itemsapp.UserTestConstants.EXPIRES_IN
import static pl.com.betacom.itemsapp.UserTestConstants.LOGIN_BODY
import static pl.com.betacom.itemsapp.UserTestConstants.TOKEN
import static pl.com.betacom.itemsapp.UserTestConstants.loginCommand

@WebMvcTest(controllers = LoginRestController)
@Import([SecurityConfig, ApiExceptionHandler, SecurityFiltersConfiguration])
@TestPropertySource(properties = [
        "app.security.login-rate-limit.attempts=100",
        "app.security.login-rate-limit.window=1m"
])
class LoginRestControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @SpringBean
    LoginUser loginUser = Mock()

    def "returns a token"() {
        when:
        def result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(LOGIN_BODY))

        then:
        1 * loginUser.login(loginCommand()) >> new LoginResult(TOKEN, EXPIRES_IN)
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.token').value(TOKEN))
                .andExpect(jsonPath('$.expiresIn').value(EXPIRES_IN))
    }

    def "rejects a missing password"() {
        when:
        def result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"login":"jan.kowalski"}'))

        then:
        0 * loginUser.login(_)
        result.andExpect(status().isBadRequest())
    }

    def "rejects invalid credentials"() {
        when:
        def result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(LOGIN_BODY))

        then:
        1 * loginUser.login(loginCommand()) >> { throw new InvalidCredentialsException() }
        result.andExpect(status().isUnauthorized())
    }
}
