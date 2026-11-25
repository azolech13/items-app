package pl.com.betacom.itemsapp

import pl.com.betacom.itemsapp.application.port.in.CreateItemCommand
import pl.com.betacom.itemsapp.application.port.in.FetchItem
import pl.com.betacom.itemsapp.domain.Item
import pl.com.betacom.itemsapp.domain.ItemRole

import java.time.Instant

import static pl.com.betacom.itemsapp.UserTestConstants.ID

class ItemTestConstants {

    static final UUID ITEM_ID = UUID.fromString("7f3c2100-ab12-4fde-8765-112233445566")
    static final UUID SHARED_ID = UUID.fromString("aaaabbbb-1234-5678-abcd-000011112222")
    static final UUID SHARED_OWNER_ID = UUID.fromString("99990000-aaaa-bbbb-cccc-ddddeeee1111")
    static final String TITLE = "Moja pierwsza notatka"
    static final String CONTENT = "Treść notatki..."
    static final String SHARED_TITLE = "Cudza notatka"
    static final String SHARED_CONTENT = "Cudza treść"
    static final Instant CREATED_AT = Instant.parse("2025-06-01T10:05:00Z")
    static final Instant SHARED_UPDATED_AT = CREATED_AT.plusSeconds(3600)
    static final int SHARED_VERSION = 1
    static final String CREATE_ITEM_BODY = '{"title":"' + TITLE + '","content":"' + CONTENT + '"}'

    static CreateItemCommand createItemCommand() {
        new CreateItemCommand(TITLE, CONTENT)
    }

    static Item item() {
        new Item(ITEM_ID, ID, TITLE, CONTENT, 0, false, CREATED_AT, CREATED_AT)
    }

    static FetchItem fetchItem() {
        new FetchItem(ITEM_ID, TITLE, CONTENT, 0, ID, ItemRole.OWNER, CREATED_AT)
    }
}
