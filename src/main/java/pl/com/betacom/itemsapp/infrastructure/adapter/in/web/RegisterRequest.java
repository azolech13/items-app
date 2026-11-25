package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record RegisterRequest(
        @NotBlank @Size(min = 3, max = 64) String login,
        @NotBlank @Size(min = 8) String password) {
}
