package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record UpdateItemRequest(@Size(min = 1, max = 255) String title, String content, @NotNull Integer version) {
}
