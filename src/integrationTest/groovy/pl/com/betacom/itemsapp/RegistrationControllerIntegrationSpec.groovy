package pl.com.betacom.itemsapp

import static pl.com.betacom.itemsapp.ItemsAppClient.read

class RegistrationControllerIntegrationSpec extends ControllerIntegrationSpec {

    def "a new person creates an account and receives an identifier"() {
        given: "the login is free and the password has at least 8 characters"
        def login = "anna-" + UUID.randomUUID().toString().substring(0, 8)

        when: "the person sends a registration"
        def response = client.post("/register", [login: login, password: PASSWORD])

        then: "the account is created and the response has the login and date, without the password"
        response.statusCode() == 201
        def account = read(response)
        account.get("login").asText() == login
        account.hasNonNull("id")
        account.hasNonNull("createdAt")
        !account.has("password")
    }

    def "registration without a login is rejected"() {
        when: "the person sends only a password"
        def response = client.post("/register", [password: PASSWORD])

        then: "the data is invalid"
        response.statusCode() == 400
    }

    def "registration without a password is rejected"() {
        when: "the person sends only a login"
        def response = client.post("/register", [login: "no-password"])

        then: "the data is invalid"
        response.statusCode() == 400
    }

    def "a login shorter than 3 characters is rejected"() {
        when: "the person sends a login of two characters"
        def response = client.post("/register", [login: "ab", password: PASSWORD])

        then: "the data is invalid"
        response.statusCode() == 400
    }

    def "a login longer than 64 characters is rejected"() {
        when: "the person sends a login of 65 characters"
        def response = client.post("/register", [login: "a" * 65, password: PASSWORD])

        then: "the data is invalid"
        response.statusCode() == 400
    }

    def "a password shorter than 8 characters is rejected"() {
        when: "the person sends a password that is too short"
        def response = client.post("/register", [login: "short-password", password: "short"])

        then: "the data is invalid"
        response.statusCode() == 400
    }

    def "a second account with the same login is rejected"() {
        given: "this login is already taken"
        def login = "taken-" + UUID.randomUUID().toString().substring(0, 8)
        client.post("/register", [login: login, password: PASSWORD])

        when: "someone creates a second account with the same login"
        def response = client.post("/register", [login: login, password: PASSWORD])

        then: "the server says the login is taken"
        response.statusCode() == 409
        detail(response) == "Login ${login} is already taken"
    }
}
