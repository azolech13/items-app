package pl.com.betacom.itemsapp

import pl.com.betacom.itemsapp.application.port.in.CreateItemCommand
import pl.com.betacom.itemsapp.domain.Item

import java.time.Instant

import static pl.com.betacom.itemsapp.UserTestConstants.ID

class ItemTestConstants {

    static final UUID ITEM_ID = UUID.fromString("7f3c2100-ab12-4fde-8765-112233445566")
    static final String TITLE = "Moja pierwsza notatka"
    static final String CONTENT = "Treść notatki..."
    static final Instant CREATED_AT = Instant.parse("2025-06-01T10:05:00Z")
    static final String CREATE_ITEM_BODY = '{"title":"' + TITLE + '","content":"' + CONTENT + '"}'

    static CreateItemCommand createItemCommand() {
        new CreateItemCommand(TITLE, CONTENT)
    }

    static Item item() {
        new Item(ITEM_ID, ID, TITLE, CONTENT, 0, false, CREATED_AT, CREATED_AT)
    }
}
