package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

record LoginRequest(@NotBlank String login, @NotBlank String password) {
}
