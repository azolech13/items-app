package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

final class UpdateItemRequestDeserializer extends JsonDeserializer<UpdateItemRequest> {

    @Override
    public UpdateItemRequest deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.readValueAsTree();
        if (node == null || !node.isObject()) {
            throw JsonMappingException.from(parser, "Update body must be a JSON object");
        }
        return new UpdateItemRequest(
                textOrNull(parser, node.get("title")),
                textOrNull(parser, node.get("content")),
                node.has("content"),
                version(parser, node.get("version")));
    }

    private static String textOrNull(JsonParser parser, JsonNode value) throws JsonMappingException {
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isTextual()) {
            throw JsonMappingException.from(parser, "Text field has an invalid type");
        }
        return value.asText();
    }

    private static Integer version(JsonParser parser, JsonNode value) throws JsonMappingException {
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isIntegralNumber()) {
            throw JsonMappingException.from(parser, "version must be an integer");
        }
        return value.intValue();
    }
}
