package pl.com.betacom.itemsapp

import static pl.com.betacom.itemsapp.ItemsAppClient.read

class LoginControllerIntegrationSpec extends ControllerIntegrationSpec {

    def "a registered person logs in and receives a token"() {
        given: "the person already has an account"
        def login = "login-" + UUID.randomUUID().toString().substring(0, 8)
        client.post("/register", [login: login, password: PASSWORD])

        when: "the person sends the login and password"
        def response = client.post("/login", [login: login, password: PASSWORD])

        then: "the person receives a token valid for one hour"
        response.statusCode() == 200
        def session = read(response)
        session.get("token").asText()
        session.get("expiresIn").asLong() == 3600L
    }

    def "login without a login is rejected"() {
        when: "the person sends only a password"
        def response = client.post("/login", [password: PASSWORD])

        then: "the data is invalid"
        response.statusCode() == 400
    }

    def "login without a password is rejected"() {
        when: "the person sends only a login"
        def response = client.post("/login", [login: "someone"])

        then: "the data is invalid"
        response.statusCode() == 400
    }

    def "login with a wrong password is rejected"() {
        given: "the person has an account"
        def login = "wrong-password-" + UUID.randomUUID().toString().substring(0, 8)
        client.post("/register", [login: login, password: PASSWORD])

        when: "the person sends a different password"
        def response = client.post("/login", [login: login, password: "other-password"])

        then: "the server refuses the login"
        response.statusCode() == 401
        detail(response) == "Invalid login or password"
    }

    def "login to an unknown account is rejected"() {
        when: "the person sends a login that does not exist"
        def response = client.post("/login", [login: "no-such-person", password: PASSWORD])

        then: "the server refuses the login"
        response.statusCode() == 401
        detail(response) == "Invalid login or password"
    }

    def "the sixth login attempt from the same address within a minute is blocked"() {
        given: "a separate application with a limit of five attempts per minute"
        def limited = environment.startAlongside("login-limit")

        when: "six login attempts arrive from one address"
        def responses = (1..6).collect {
            limited.post("/login", [login: "missing-account", password: PASSWORD])
        }
        def sixth = responses[5]
        def retryAfter = sixth.headers().firstValue("retry-after")

        then: "the first five are refused and the sixth says to wait"
        responses[0..4].every { it.statusCode() == 401 }
        sixth.statusCode() == 429
        retryAfter.present
        retryAfter.get().toLong() in (1L..60L)
    }
}
