package pl.com.betacom.itemsapp.domain;

import java.time.Instant;
import java.util.UUID;

public record User(UUID id, String login, String passwordHash, Instant createdAt) {
}
