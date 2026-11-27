package pl.com.betacom.itemsapp

import com.fasterxml.jackson.databind.JsonNode
import java.net.http.HttpResponse
import spock.lang.Shared
import spock.lang.Specification

import static pl.com.betacom.itemsapp.ItemsAppClient.read
import static pl.com.betacom.itemsapp.ItemsAppClient.requireJson

abstract class ControllerIntegrationSpec extends Specification {

    protected static final String PASSWORD = "haslo1234"

    private static ItemsAppEnvironment sharedEnvironment
    private static ItemsAppClient sharedClient

    @Shared
    ItemsAppEnvironment environment

    @Shared
    ItemsAppClient client

    def setupSpec() {
        if (sharedEnvironment == null) {
            ItemsAppEnvironment created = ItemsAppEnvironment.start()
            try {
                sharedClient = created.startApp("items-app", ["APP_SECURITY_LOGIN_RATE_LIMIT_ATTEMPTS": "1000"])
                sharedEnvironment = created
                Runtime.getRuntime().addShutdownHook(new Thread({ sharedEnvironment?.close() }))
            } catch (RuntimeException exception) {
                created.close()
                throw exception
            }
        }
        environment = sharedEnvironment
        client = sharedClient
    }

    protected Account register(String role) {
        String login = role + "-" + UUID.randomUUID().toString().substring(0, 8)
        String id = requireJson(client.post("/register", [login: login, password: PASSWORD]), 201).get("id").asText()
        String token = requireJson(client.post("/login", [login: login, password: PASSWORD]), 200).get("token").asText()
        new Account(id, login, token)
    }

    protected JsonNode createItem(String token, String title, String content = "Note content") {
        requireJson(client.post("/items", [title: title, content: content], token), 201)
    }

    protected JsonNode listItems(String token) {
        requireJson(client.get("/items", token), 200)
    }

    protected static JsonNode itemByTitle(JsonNode items, String title) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).get("title").asText() == title) {
                return items.get(i)
            }
        }
        throw new AssertionError("The list has no note titled: " + title)
    }

    protected static boolean listContains(JsonNode items, String title) {
        (0..<items.size()).any { items.get(it).get("title").asText() == title }
    }

    protected static String detail(HttpResponse<String> response) {
        read(response).get("detail").asText()
    }

    protected static class Account {
        final String id
        final String login
        final String token

        Account(String id, String login, String token) {
            this.id = id
            this.login = login
            this.token = token
        }
    }
}
