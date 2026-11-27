package pl.com.betacom.itemsapp

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class ItemsAppClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(20)
    private static final ObjectMapper JSON = new ObjectMapper()

    private final String baseUrl
    private final HttpClient http = HttpClient.newHttpClient()

    ItemsAppClient(String baseUrl) {
        this.baseUrl = baseUrl
    }

    HttpResponse<String> post(String path, Map body, String token = null) {
        send("POST", path, body, token)
    }

    HttpResponse<String> patch(String path, Map body, String token = null) {
        send("PATCH", path, body, token)
    }

    HttpResponse<String> get(String path, String token = null) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(TIMEOUT)
                .GET()
        if (token != null) {
            request.header("Authorization", "Bearer " + token)
        }
        http.send(request.build(), HttpResponse.BodyHandlers.ofString())
    }

    HttpResponse<String> delete(String path, String token = null) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(TIMEOUT)
                .DELETE()
        if (token != null) {
            request.header("Authorization", "Bearer " + token)
        }
        http.send(request.build(), HttpResponse.BodyHandlers.ofString())
    }

    static JsonNode read(HttpResponse<String> response) {
        JSON.readTree(response.body())
    }

    static JsonNode requireJson(HttpResponse<String> response, int expectedStatus) {
        requireStatus(response, expectedStatus)
        JSON.readTree(response.body())
    }

    static void requireStatus(HttpResponse<String> response, int expectedStatus) {
        if (response.statusCode() != expectedStatus) {
            throw new AssertionError("Expected HTTP ${expectedStatus}, got ${response.statusCode()}: ${response.body()}")
        }
    }

    private HttpResponse<String> send(String method, String path, Map body, String token) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body)))
        if (token != null) {
            request.header("Authorization", "Bearer " + token)
        }
        http.send(request.build(), HttpResponse.BodyHandlers.ofString())
    }
}
