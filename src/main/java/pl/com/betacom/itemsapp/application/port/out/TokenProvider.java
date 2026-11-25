package pl.com.betacom.itemsapp.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface TokenProvider {

    String issue(UUID userId);

    long expirationSeconds();

    Optional<UUID> parseUserId(String token);
}
