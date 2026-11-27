package pl.com.betacom.itemsapp

import com.fasterxml.jackson.databind.JsonNode

import static pl.com.betacom.itemsapp.ItemsAppClient.read
import static pl.com.betacom.itemsapp.ItemsAppClient.requireJson

class ItemControllerIntegrationSpec extends ControllerIntegrationSpec {

    def "a signed-in person creates a note and becomes its owner"() {
        given: "the person is signed in"
        def owner = register("owner")

        when: "the person creates a note with a title and content"
        def response = client.post("/items", [title: "Shopping", content: "milk and bread"], owner.token)

        then: "the note is created at version zero and belongs to that person"
        response.statusCode() == 201
        def note = read(response)
        note.get("title").asText() == "Shopping"
        note.get("content").asText() == "milk and bread"
        note.get("version").asInt() == 0
        note.get("ownerId").asText() == owner.id
        note.hasNonNull("id")
        note.hasNonNull("createdAt")
        note.hasNonNull("updatedAt")
    }

    def "a note can be created without content"() {
        given: "the person is signed in"
        def owner = register("owner")

        when: "the person creates a note with only a title"
        def response = client.post("/items", [title: "Title only", content: null], owner.token)

        then: "the note is created and the content is empty"
        response.statusCode() == 201
        read(response).get("content").isNull()
    }

    def "a note without a title is rejected"() {
        given: "the person is signed in"
        def owner = register("owner")

        when: "the person sends a note without a title"
        def response = client.post("/items", [content: "content only"], owner.token)

        then: "the data is invalid"
        response.statusCode() == 400
    }

    def "a note with an empty title is rejected"() {
        given: "the person is signed in"
        def owner = register("owner")

        when: "the person sends an empty title"
        def response = client.post("/items", [title: "", content: "content"], owner.token)

        then: "the data is invalid"
        response.statusCode() == 400
    }

    def "a note with a title longer than 255 characters is rejected"() {
        given: "the person is signed in"
        def owner = register("owner")

        when: "the person sends a title of 256 characters"
        def response = client.post("/items", [title: "a" * 256, content: "content"], owner.token)

        then: "the data is invalid"
        response.statusCode() == 400
    }

    def "a person who is not signed in cannot create a note"() {
        when: "the person sends a note without signing in"
        def response = client.post("/items", [title: "Shopping", content: "milk"])

        then: "the server refuses"
        response.statusCode() == 401
    }

    def "a person with an invalid token cannot create a note"() {
        when: "the person sends a note with a token that cannot be read"
        def response = client.post("/items", [title: "Shopping", content: "milk"], "this-is-not-a-token")

        then: "the server refuses"
        response.statusCode() == 401
    }

    def "a new account sees an empty list of notes"() {
        given: "the person has just created an account"
        def owner = register("owner")

        when: "the person asks for their notes"
        def response = client.get("/items", owner.token)

        then: "the list is empty"
        response.statusCode() == 200
        read(response).size() == 0
    }

    def "the owner sees their note with the owner role"() {
        given: "the owner has a note"
        def owner = register("owner")
        createItem(owner.token, "My note", "content")

        when: "the owner opens the list"
        def notes = listItems(owner.token)

        then: "the note is visible with the owner role"
        itemByTitle(notes, "My note").get("myRole").asText() == "OWNER"
    }

    def "an editor and a reader see a shared note with their own roles"() {
        given: "the owner shared a note with an editor and a reader"
        def owner = register("owner")
        def editor = register("editor")
        def reader = register("reader")
        def note = createItem(owner.token, "Shared", "together")
        def id = note.get("id").asText()
        client.post("/items/$id/share", [userId: editor.id, role: "EDITOR"], owner.token)
        client.post("/items/$id/share", [userId: reader.id, role: "VIEWER"], owner.token)

        when: "each person opens their own list"
        def editorList = listItems(editor.token)
        def readerList = listItems(reader.token)

        then: "the editor sees the editor role and the reader sees the reader role"
        itemByTitle(editorList, "Shared").get("myRole").asText() == "EDITOR"
        itemByTitle(readerList, "Shared").get("myRole").asText() == "VIEWER"
    }

    def "a stranger does not see someone else's note"() {
        given: "the owner has a private note and another person was not given access"
        def owner = register("owner")
        def stranger = register("stranger")
        createItem(owner.token, "Private", "secret")

        when: "the stranger opens their list"
        def notes = listItems(stranger.token)

        then: "that note is not there"
        !listContains(notes, "Private")
    }

    def "a deleted note disappears from the lists of the owner and people with access"() {
        given: "the note was shared with an editor and the owner deleted it"
        def owner = register("owner")
        def editor = register("editor")
        def note = createItem(owner.token, "To the bin", "old")
        def id = note.get("id").asText()
        client.post("/items/$id/share", [userId: editor.id, role: "EDITOR"], owner.token)
        client.delete("/items/$id", owner.token)

        when: "the owner and the editor open their lists"
        def ownerList = listItems(owner.token)
        def editorList = listItems(editor.token)

        then: "the note is on neither list"
        !listContains(ownerList, "To the bin")
        !listContains(editorList, "To the bin")
    }

    def "a person who is not signed in cannot see the list of notes"() {
        when: "the person asks for the list without signing in"
        def response = client.get("/items")

        then: "the server refuses"
        response.statusCode() == 401
    }

    def "the owner updates a note and receives a higher version number"() {
        given: "the owner has a note at version zero"
        def owner = register("owner")
        def note = createItem(owner.token, "Draft", "first version")

        when: "the owner saves new content with the current version"
        def response = client.patch("/items/${note.get('id').asText()}", [
                title: "Draft", content: "second version", version: 0
        ], owner.token)

        then: "the save succeeds and the version increases"
        response.statusCode() == 200
        def updated = read(response)
        updated.get("content").asText() == "second version"
        updated.get("version").asInt() == 1
        updated.hasNonNull("updatedAt")
    }

    def "an editor updates the owner's note"() {
        given: "the editor has access to the note"
        def owner = register("owner")
        def editor = register("editor")
        def note = createItem(owner.token, "Shared", "together")
        def id = note.get("id").asText()
        client.post("/items/$id/share", [userId: editor.id, role: "EDITOR"], owner.token)

        when: "the editor saves new content"
        def response = client.patch("/items/$id", [
                title: "Shared after edit", content: "changed", version: 0
        ], editor.token)

        then: "the save succeeds"
        response.statusCode() == 200
        read(response).get("title").asText() == "Shared after edit"
        read(response).get("version").asInt() == 1
    }

    def "updating only the title leaves the content unchanged"() {
        given: "the note has content"
        def owner = register("owner")
        def note = createItem(owner.token, "Shopping", "milk and bread")

        when: "the owner sends only a new title and the current version"
        def response = client.patch("/items/${note.get('id').asText()}", [
                title: "Shopping for tomorrow", version: 0
        ], owner.token)

        then: "the title changes and the content stays"
        response.statusCode() == 200
        def updated = read(response)
        updated.get("title").asText() == "Shopping for tomorrow"
        updated.get("content").asText() == "milk and bread"
    }

    def "updating only the content leaves the title unchanged"() {
        given: "the note has a title"
        def owner = register("owner")
        def note = createItem(owner.token, "Shopping", "milk")

        when: "the owner sends only new content and the current version"
        def response = client.patch("/items/${note.get('id').asText()}", [
                content: "milk and bread", version: 0
        ], owner.token)

        then: "the content changes and the title stays"
        response.statusCode() == 200
        def updated = read(response)
        updated.get("title").asText() == "Shopping"
        updated.get("content").asText() == "milk and bread"
    }

    def "sending empty content clears the note"() {
        given: "the note has content"
        def owner = register("owner")
        def note = createItem(owner.token, "Shopping", "milk")

        when: "the owner sends the content as an empty value"
        def response = client.patch("/items/${note.get('id').asText()}", [
                content: null, version: 0
        ], owner.token)

        then: "the content is cleared"
        response.statusCode() == 200
        read(response).get("content").isNull()
        read(response).get("title").asText() == "Shopping"
    }

    def "an update without a version number is rejected"() {
        given: "the owner has a note"
        def owner = register("owner")
        def note = createItem(owner.token, "Draft", "content")

        when: "the owner sends an update without a version"
        def response = client.patch("/items/${note.get('id').asText()}", [
                title: "New title", content: "new"
        ], owner.token)

        then: "the data is invalid"
        response.statusCode() == 400
    }

    def "an update with an empty title is rejected"() {
        given: "the owner has a note"
        def owner = register("owner")
        def note = createItem(owner.token, "Draft", "content")

        when: "the owner sends an empty title"
        def response = client.patch("/items/${note.get('id').asText()}", [
                title: "", version: 0
        ], owner.token)

        then: "the data is invalid"
        response.statusCode() == 400
    }

    def "a reader cannot update a note"() {
        given: "the reader has read-only access"
        def owner = register("owner")
        def reader = register("reader")
        def note = createItem(owner.token, "Shared", "together")
        def id = note.get("id").asText()
        client.post("/items/$id/share", [userId: reader.id, role: "VIEWER"], owner.token)

        when: "the reader tries to save a change"
        def response = client.patch("/items/$id", [title: "attempt", content: "attempt", version: 0], reader.token)

        then: "the server refuses"
        response.statusCode() == 403
        detail(response) == "No permission to edit this item"
    }

    def "a stranger cannot update someone else's note"() {
        given: "the note belongs to someone else"
        def owner = register("owner")
        def stranger = register("stranger")
        def note = createItem(owner.token, "Private", "secret")

        when: "the stranger tries to change it"
        def response = client.patch("/items/${note.get('id').asText()}", [
                title: "attempt", content: "attempt", version: 0
        ], stranger.token)

        then: "the server refuses"
        response.statusCode() == 403
        detail(response) == "No permission to edit this item"
    }

    def "an update of a missing note is rejected"() {
        given: "the person is signed in"
        def owner = register("owner")
        def missingId = UUID.randomUUID()

        when: "the person updates a note that does not exist"
        def response = client.patch("/items/$missingId", [title: "x", content: "y", version: 0], owner.token)

        then: "the server does not find it"
        response.statusCode() == 404
        detail(response) == "Item ${missingId} was not found"
    }

    def "an update of a deleted note is rejected"() {
        given: "the owner deleted the note"
        def owner = register("owner")
        def note = createItem(owner.token, "Deleted", "old")
        def id = note.get("id").asText()
        client.delete("/items/$id", owner.token)

        when: "the owner tries to update it again"
        def response = client.patch("/items/$id", [title: "again", content: "again", version: 0], owner.token)

        then: "the server treats it as missing"
        response.statusCode() == 404
        detail(response) == "Item ${id} was not found"
    }

    def "an update from an old version is rejected and reports the current version"() {
        given: "the note is at version zero"
        def owner = register("owner")
        def note = createItem(owner.token, "Draft", "content")

        when: "the owner sends a version other than the one on the note"
        def response = client.patch("/items/${note.get('id').asText()}", [
                title: "New", content: "new", version: 5
        ], owner.token)

        then: "the save fails and the response contains the current version"
        response.statusCode() == 409
        detail(response) == "Current version is 0"
    }

    def "a person who is not signed in cannot update a note"() {
        given: "the note exists"
        def owner = register("owner")
        def note = createItem(owner.token, "Draft", "content")

        when: "someone who is not signed in sends an update"
        def response = client.patch("/items/${note.get('id').asText()}", [title: "x", content: "y", version: 0])

        then: "the server refuses"
        response.statusCode() == 401
    }

    def "the owner deletes a note and it disappears from the list"() {
        given: "the owner has a note"
        def owner = register("owner")
        def note = createItem(owner.token, "To delete", "content")

        when: "the owner deletes it"
        def response = client.delete("/items/${note.get('id').asText()}", owner.token)

        then: "the deletion succeeds and the note is gone from the list"
        response.statusCode() == 204
        !listContains(listItems(owner.token), "To delete")
    }

    def "an editor cannot delete a note"() {
        given: "the editor can update the note but is not the owner"
        def owner = register("owner")
        def editor = register("editor")
        def note = createItem(owner.token, "Shared", "together")
        def id = note.get("id").asText()
        client.post("/items/$id/share", [userId: editor.id, role: "EDITOR"], owner.token)

        when: "the editor tries to delete it"
        def response = client.delete("/items/$id", editor.token)

        then: "the server refuses"
        response.statusCode() == 403
        detail(response) == "No permission to delete this item"
    }

    def "a reader cannot delete a note"() {
        given: "the reader has read-only access"
        def owner = register("owner")
        def reader = register("reader")
        def note = createItem(owner.token, "Shared", "together")
        def id = note.get("id").asText()
        client.post("/items/$id/share", [userId: reader.id, role: "VIEWER"], owner.token)

        when: "the reader tries to delete it"
        def response = client.delete("/items/$id", reader.token)

        then: "the server refuses"
        response.statusCode() == 403
        detail(response) == "No permission to delete this item"
    }

    def "a stranger cannot delete someone else's note"() {
        given: "the note belongs to someone else"
        def owner = register("owner")
        def stranger = register("stranger")
        def note = createItem(owner.token, "Private", "secret")

        when: "the stranger tries to delete it"
        def response = client.delete("/items/${note.get('id').asText()}", stranger.token)

        then: "the server refuses"
        response.statusCode() == 403
        detail(response) == "No permission to delete this item"
    }

    def "deleting a missing note is rejected"() {
        given: "the person is signed in"
        def owner = register("owner")
        def missingId = UUID.randomUUID()

        when: "the person deletes a note that does not exist"
        def response = client.delete("/items/$missingId", owner.token)

        then: "the server does not find it"
        response.statusCode() == 404
        detail(response) == "Item ${missingId} was not found"
    }

    def "deleting a note a second time is rejected"() {
        given: "the owner already deleted the note"
        def owner = register("owner")
        def note = createItem(owner.token, "Already deleted", "content")
        def id = note.get("id").asText()
        client.delete("/items/$id", owner.token)

        when: "the owner deletes it again"
        def response = client.delete("/items/$id", owner.token)

        then: "the server treats it as missing"
        response.statusCode() == 404
        detail(response) == "Item ${id} was not found"
    }

    def "a person who is not signed in cannot delete a note"() {
        given: "the note exists"
        def owner = register("owner")
        def note = createItem(owner.token, "Draft", "content")

        when: "someone who is not signed in tries to delete it"
        def response = client.delete("/items/${note.get('id').asText()}")

        then: "the server refuses"
        response.statusCode() == 401
    }

    def "history shows creation, updates and deletion and who made them"() {
        given: "the owner and an editor create, update and delete a note in turn"
        def owner = register("owner")
        def editor = register("editor")
        def created = createItem(owner.token, "My first note", "Note content")
        def id = created.get("id").asText()
        client.post("/items/$id/share", [userId: editor.id, role: "EDITOR"], owner.token)
        def afterEditor = requireJson(client.patch("/items/$id", [
                title: "Updated title", content: "New content", version: 0
        ], editor.token), 200)
        client.patch("/items/$id", [
                title: "Title after the second edit", content: "Content after the second edit", version: afterEditor.get("version").asInt()
        ], owner.token)
        client.delete("/items/$id", owner.token)

        when: "the owner opens the history"
        def response = client.get("/items/$id/history", owner.token)

        then: "creation, two updates and deletion are visible, oldest first, with the author of each change"
        response.statusCode() == 200
        JsonNode revisions = read(response)
        revisions.size() == 4
        field(revisions, "revisionType") == ["ADD", "MOD", "MOD", "DEL"]
        field(revisions, "changedBy") == [owner.login, editor.login, owner.login, owner.login]
        field(revisions, "title") == ["My first note", "Updated title", "Title after the second edit", "Title after the second edit"]
        field(revisions, "content") == ["Note content", "New content", "Content after the second edit", "Content after the second edit"]
        (0..<(revisions.size() - 1)).every {
            revisions.get(it).get("revision").asInt() < revisions.get(it + 1).get("revision").asInt()
        }
        (0..<revisions.size()).every { revisions.get(it).hasNonNull("timestamp") }
    }

    def "an editor can read the history of a note"() {
        given: "the editor has access to the note"
        def owner = register("owner")
        def editor = register("editor")
        def note = createItem(owner.token, "Shared", "together")
        def id = note.get("id").asText()
        client.post("/items/$id/share", [userId: editor.id, role: "EDITOR"], owner.token)

        when: "the editor opens the history"
        def response = client.get("/items/$id/history", editor.token)

        then: "the editor sees the creation of the note"
        response.statusCode() == 200
        read(response).get(0).get("revisionType").asText() == "ADD"
        read(response).get(0).get("changedBy").asText() == owner.login
    }

    def "a reader can read the history of a note"() {
        given: "the reader has access to the note"
        def owner = register("owner")
        def reader = register("reader")
        def note = createItem(owner.token, "Shared", "together")
        def id = note.get("id").asText()
        client.post("/items/$id/share", [userId: reader.id, role: "VIEWER"], owner.token)

        when: "the reader opens the history"
        def response = client.get("/items/$id/history", reader.token)

        then: "the reader sees the creation of the note"
        response.statusCode() == 200
        read(response).size() == 1
    }

    def "a stranger cannot read the history"() {
        given: "the note belongs to someone else"
        def owner = register("owner")
        def stranger = register("stranger")
        def note = createItem(owner.token, "Private", "secret")

        when: "the stranger opens the history"
        def response = client.get("/items/${note.get('id').asText()}/history", stranger.token)

        then: "the server refuses"
        response.statusCode() == 403
        detail(response) == "No permission to read this item"
    }

    def "history of a missing note is rejected"() {
        given: "the person is signed in"
        def owner = register("owner")
        def missingId = UUID.randomUUID()

        when: "the person asks for the history of a note that does not exist"
        def response = client.get("/items/$missingId/history", owner.token)

        then: "the server does not find it"
        response.statusCode() == 404
        detail(response) == "Item ${missingId} was not found"
    }

    def "a person who is not signed in cannot read the history"() {
        given: "the note exists"
        def owner = register("owner")
        def note = createItem(owner.token, "Draft", "content")

        when: "someone who is not signed in asks for the history"
        def response = client.get("/items/${note.get('id').asText()}/history")

        then: "the server refuses"
        response.statusCode() == 401
    }

    private static List<String> field(JsonNode revisions, String name) {
        (0..<revisions.size()).collect { revisions.get(it).get(name).asText() }
    }
}
