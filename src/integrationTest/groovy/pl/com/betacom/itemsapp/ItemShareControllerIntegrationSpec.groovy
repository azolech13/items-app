package pl.com.betacom.itemsapp

import static pl.com.betacom.itemsapp.ItemsAppClient.read

class ItemShareControllerIntegrationSpec extends ControllerIntegrationSpec {

    def "the owner grants an editor access to a note"() {
        given: "the owner has a note and the editor has an account"
        def owner = register("owner")
        def editor = register("editor")
        def note = createItem(owner.token, "Shared", "together")

        when: "the owner grants the editor role"
        def response = client.post("/items/${note.get('id').asText()}/share", [
                userId: editor.id, role: "EDITOR"
        ], owner.token)

        then: "access is granted"
        response.statusCode() == 201
        def grant = read(response)
        grant.get("itemId").asText() == note.get("id").asText()
        grant.get("userId").asText() == editor.id
        grant.get("role").asText() == "EDITOR"
        grant.hasNonNull("grantedAt")
    }

    def "the owner grants a reader access to a note"() {
        given: "the owner has a note and the reader has an account"
        def owner = register("owner")
        def reader = register("reader")
        def note = createItem(owner.token, "Shared", "together")

        when: "the owner grants the reader role"
        def response = client.post("/items/${note.get('id').asText()}/share", [
                userId: reader.id, role: "VIEWER"
        ], owner.token)

        then: "access is granted"
        response.statusCode() == 201
        read(response).get("role").asText() == "VIEWER"
    }

    def "the owner changes a role that was already granted"() {
        given: "the reader already has access to the note"
        def owner = register("owner")
        def reader = register("reader")
        def note = createItem(owner.token, "Shared", "together")
        def id = note.get("id").asText()
        client.post("/items/$id/share", [userId: reader.id, role: "VIEWER"], owner.token)

        when: "the owner changes the role to editor"
        def response = client.post("/items/$id/share", [userId: reader.id, role: "EDITOR"], owner.token)

        then: "the role is updated"
        response.statusCode() == 200
        read(response).get("role").asText() == "EDITOR"
        read(response).hasNonNull("grantedAt")
    }

    def "the owner cannot grant access to themselves"() {
        given: "the owner has a note"
        def owner = register("owner")
        def note = createItem(owner.token, "Own note", "content")

        when: "the owner tries to share it with themselves"
        def response = client.post("/items/${note.get('id').asText()}/share", [
                userId: owner.id, role: "EDITOR"
        ], owner.token)

        then: "the server rejects that grant"
        response.statusCode() == 400
        detail(response) == "Cannot grant access to yourself"
    }

    def "granting access without a person is rejected"() {
        given: "the owner has a note"
        def owner = register("owner")
        def note = createItem(owner.token, "Shared", "together")

        when: "the owner sends only a role"
        def response = client.post("/items/${note.get('id').asText()}/share", [role: "EDITOR"], owner.token)

        then: "the data is invalid"
        response.statusCode() == 400
    }

    def "granting access without a role is rejected"() {
        given: "the owner has a note and names another person"
        def owner = register("owner")
        def editor = register("editor")
        def note = createItem(owner.token, "Shared", "together")

        when: "the owner does not send a role"
        def response = client.post("/items/${note.get('id').asText()}/share", [userId: editor.id], owner.token)

        then: "the data is invalid"
        response.statusCode() == 400
    }

    def "a role other than editor or reader is rejected"() {
        given: "the owner has a note and names another person"
        def owner = register("owner")
        def editor = register("editor")
        def note = createItem(owner.token, "Shared", "together")

        when: "the owner tries to grant the owner role"
        def response = client.post("/items/${note.get('id').asText()}/share", [
                userId: editor.id, role: "OWNER"
        ], owner.token)

        then: "the data is invalid and the response lists the accepted roles"
        response.statusCode() == 400
        read(response).get("detail").asText() == "Request validation failed"
        read(response).get("errors").get(0).asText() == "role: accepts only VIEWER, EDITOR"
    }

    def "an editor cannot grant access"() {
        given: "the editor has access to the note and a third person does not"
        def owner = register("owner")
        def editor = register("editor")
        def stranger = register("stranger")
        def note = createItem(owner.token, "Shared", "together")
        def id = note.get("id").asText()
        client.post("/items/$id/share", [userId: editor.id, role: "EDITOR"], owner.token)

        when: "the editor tries to grant access to someone"
        def response = client.post("/items/$id/share", [userId: stranger.id, role: "VIEWER"], editor.token)

        then: "the server refuses"
        response.statusCode() == 403
        detail(response) == "No permission to share this item"
    }

    def "a reader cannot grant access"() {
        given: "the reader has read-only access"
        def owner = register("owner")
        def reader = register("reader")
        def stranger = register("stranger")
        def note = createItem(owner.token, "Shared", "together")
        def id = note.get("id").asText()
        client.post("/items/$id/share", [userId: reader.id, role: "VIEWER"], owner.token)

        when: "the reader tries to grant access to someone"
        def response = client.post("/items/$id/share", [userId: stranger.id, role: "VIEWER"], reader.token)

        then: "the server refuses"
        response.statusCode() == 403
        detail(response) == "No permission to share this item"
    }

    def "a stranger cannot grant access"() {
        given: "the note belongs to someone else"
        def owner = register("owner")
        def stranger = register("stranger")
        def someone = register("someone")
        def note = createItem(owner.token, "Private", "secret")

        when: "the stranger tries to share it"
        def response = client.post("/items/${note.get('id').asText()}/share", [
                userId: someone.id, role: "VIEWER"
        ], stranger.token)

        then: "the server refuses"
        response.statusCode() == 403
        detail(response) == "No permission to share this item"
    }

    def "granting access to a missing note is rejected"() {
        given: "two people have accounts"
        def owner = register("owner")
        def editor = register("editor")
        def missingId = UUID.randomUUID()

        when: "the owner shares a note that does not exist"
        def response = client.post("/items/$missingId/share", [userId: editor.id, role: "EDITOR"], owner.token)

        then: "the server does not find it"
        response.statusCode() == 404
        detail(response) == "Item ${missingId} was not found"
    }

    def "granting access to a missing person is rejected"() {
        given: "the owner has a note"
        def owner = register("owner")
        def note = createItem(owner.token, "Shared", "together")
        def missingPerson = UUID.randomUUID()

        when: "the owner names a person who does not exist"
        def response = client.post("/items/${note.get('id').asText()}/share", [
                userId: missingPerson.toString(), role: "EDITOR"
        ], owner.token)

        then: "the server does not find that person"
        response.statusCode() == 404
        detail(response) == "User ${missingPerson} was not found"
    }

    def "a person who is not signed in cannot grant access"() {
        given: "the note exists"
        def owner = register("owner")
        def editor = register("editor")
        def note = createItem(owner.token, "Shared", "together")

        when: "someone who is not signed in grants access"
        def response = client.post("/items/${note.get('id').asText()}/share", [userId: editor.id, role: "EDITOR"])

        then: "the server refuses"
        response.statusCode() == 401
    }

    def "the owner revokes access and the person loses the note and its history"() {
        given: "the reader can see the shared note"
        def owner = register("owner")
        def reader = register("reader")
        def note = createItem(owner.token, "Shared", "together")
        def id = note.get("id").asText()
        client.post("/items/$id/share", [userId: reader.id, role: "VIEWER"], owner.token)

        when: "the owner revokes that access"
        def response = client.delete("/items/$id/share/${reader.id}", owner.token)

        then: "access is gone, the note is missing from the reader's list and the history is unavailable"
        response.statusCode() == 204
        !listContains(listItems(reader.token), "Shared")
        def history = client.get("/items/$id/history", reader.token)
        history.statusCode() == 403
        detail(history) == "No permission to read this item"
    }

    def "an editor cannot revoke access"() {
        given: "an editor and a reader have access"
        def owner = register("owner")
        def editor = register("editor")
        def reader = register("reader")
        def note = createItem(owner.token, "Shared", "together")
        def id = note.get("id").asText()
        client.post("/items/$id/share", [userId: editor.id, role: "EDITOR"], owner.token)
        client.post("/items/$id/share", [userId: reader.id, role: "VIEWER"], owner.token)

        when: "the editor tries to revoke the reader's access"
        def response = client.delete("/items/$id/share/${reader.id}", editor.token)

        then: "the server refuses"
        response.statusCode() == 403
        detail(response) == "No permission to revoke this item"
    }

    def "a reader cannot revoke access"() {
        given: "a reader and an editor have access"
        def owner = register("owner")
        def editor = register("editor")
        def reader = register("reader")
        def note = createItem(owner.token, "Shared", "together")
        def id = note.get("id").asText()
        client.post("/items/$id/share", [userId: editor.id, role: "EDITOR"], owner.token)
        client.post("/items/$id/share", [userId: reader.id, role: "VIEWER"], owner.token)

        when: "the reader tries to revoke the editor's access"
        def response = client.delete("/items/$id/share/${editor.id}", reader.token)

        then: "the server refuses"
        response.statusCode() == 403
        detail(response) == "No permission to revoke this item"
    }

    def "a stranger cannot revoke access"() {
        given: "the reader has access and a third person does not"
        def owner = register("owner")
        def reader = register("reader")
        def stranger = register("stranger")
        def note = createItem(owner.token, "Shared", "together")
        def id = note.get("id").asText()
        client.post("/items/$id/share", [userId: reader.id, role: "VIEWER"], owner.token)

        when: "the stranger tries to revoke that access"
        def response = client.delete("/items/$id/share/${reader.id}", stranger.token)

        then: "the server refuses"
        response.statusCode() == 403
        detail(response) == "No permission to revoke this item"
    }

    def "revoking access to a missing note is rejected"() {
        given: "two people have accounts"
        def owner = register("owner")
        def reader = register("reader")
        def missingId = UUID.randomUUID()

        when: "the owner revokes access to a note that does not exist"
        def response = client.delete("/items/$missingId/share/${reader.id}", owner.token)

        then: "the server does not find it"
        response.statusCode() == 404
        detail(response) == "Item ${missingId} was not found"
    }

    def "revoking access that was never granted is rejected"() {
        given: "the note was not shared with this person"
        def owner = register("owner")
        def reader = register("reader")
        def note = createItem(owner.token, "Shared", "together")
        def id = note.get("id").asText()

        when: "the owner tries to revoke access anyway"
        def response = client.delete("/items/$id/share/${reader.id}", owner.token)

        then: "the server does not find that access"
        response.statusCode() == 404
        detail(response) == "Access for user ${reader.id} on item ${id} was not found"
    }

    def "a person who is not signed in cannot revoke access"() {
        given: "the reader has access"
        def owner = register("owner")
        def reader = register("reader")
        def note = createItem(owner.token, "Shared", "together")
        def id = note.get("id").asText()
        client.post("/items/$id/share", [userId: reader.id, role: "VIEWER"], owner.token)

        when: "someone who is not signed in tries to revoke that access"
        def response = client.delete("/items/$id/share/${reader.id}")

        then: "the server refuses"
        response.statusCode() == 401
    }
}
