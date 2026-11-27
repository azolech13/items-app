package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonDeserialize(using = UpdateItemRequestDeserializer.class)
record UpdateItemRequest(
        @Size(min = 1, max = 255) String title,
        String content,
        boolean contentSpecified,
        @NotNull Integer version) {
}
