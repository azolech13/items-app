package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import java.time.Instant;
import java.util.UUID;

record RegisterResponse(UUID id, String login, Instant createdAt) {
}
