package pl.com.betacom.itemsapp

import com.fasterxml.jackson.databind.JsonNode
import java.net.http.HttpResponse
import spock.lang.Shared
import spock.lang.Specification

import static pl.com.betacom.itemsapp.ItemsAppClient.requireJson
import static pl.com.betacom.itemsapp.ItemsAppClient.requireStatus

class ItemsAppIntegrationSpec extends Specification {

    @Shared
    ItemsAppEnvironment environment

    def setupSpec() {
        environment = ItemsAppEnvironment.start()
    }

    def cleanup() {
        environment?.stopApp()
    }

    def cleanupSpec() {
        environment?.close()
    }

    def "records every revision through the history endpoint"() {
        given:
        def client = environment.startApp("history")
        def ownerLogin = "history.owner"
        def editorLogin = "history.editor"
        def password = "password1"
        def title = "Moja pierwsza notatka"
        def content = "Treść notatki"
        def editedTitle = "Zaktualizowany tytuł"
        def editedContent = "Nowa treść"
        def finalTitle = "Tytuł po drugiej edycji"
        def finalContent = "Treść po drugiej edycji"

        when:
        requireStatus(client.post("/register", [login: ownerLogin, password: password]), 201)
        def editorId = requireJson(client.post("/register", [login: editorLogin, password: password]), 201).get("id").asText()
        def ownerToken = requireJson(client.post("/login", [login: ownerLogin, password: password]), 200).get("token").asText()
        def editorToken = requireJson(client.post("/login", [login: editorLogin, password: password]), 200).get("token").asText()

        def created = requireJson(client.post("/items", [title: title, content: content], ownerToken), 201)
        def itemId = created.get("id").asText()
        requireStatus(client.post("/items/$itemId/share", [userId: editorId, role: "EDITOR"], ownerToken), 201)

        def firstEdit = requireJson(client.patch("/items/$itemId", [
                title: editedTitle, content: editedContent, version: created.get("version").asInt()
        ], editorToken), 200)
        requireStatus(client.patch("/items/$itemId", [
                title: finalTitle, content: finalContent, version: firstEdit.get("version").asInt()
        ], ownerToken), 200)
        requireStatus(client.delete("/items/$itemId", ownerToken), 204)

        JsonNode revisions = requireJson(client.get("/items/$itemId/history", ownerToken), 200)

        then:
        revisions.size() == 4
        field(revisions, "revisionType") == ["ADD", "MOD", "MOD", "DEL"]
        field(revisions, "changedBy") == [ownerLogin, editorLogin, ownerLogin, ownerLogin]
        field(revisions, "title") == [title, editedTitle, finalTitle, finalTitle]
        field(revisions, "content") == [content, editedContent, finalContent, finalContent]
        (0..<(revisions.size() - 1)).every {
            revisions.get(it).get("revision").asInt() < revisions.get(it + 1).get("revision").asInt()
        }
        (0..<revisions.size()).every { revisions.get(it).hasNonNull("timestamp") }
    }

    def "returns 429 with Retry-After on the sixth login"() {
        given:
        def client = environment.startApp("rate-limit")

        when:
        List<HttpResponse<String>> responses = (1..6).collect {
            client.post("/login", [login: "missing.user", password: "password1"])
        }
        def rejected = responses[5]
        def retryAfter = rejected.headers().firstValue("retry-after")

        then:
        responses[0..4].every { it.statusCode() == 401 }
        rejected.statusCode() == 429
        retryAfter.present
        retryAfter.get().toLong() in (1L..60L)
    }

    private static List<String> field(JsonNode revisions, String name) {
        (0..<revisions.size()).collect { revisions.get(it).get(name).asText() }
    }
}
