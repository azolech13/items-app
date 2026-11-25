package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record ItemRequest(@NotBlank @Size(min = 1, max = 255) String title, String content) {
}
