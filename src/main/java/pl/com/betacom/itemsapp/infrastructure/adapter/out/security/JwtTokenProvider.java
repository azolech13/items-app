package pl.com.betacom.itemsapp.infrastructure.adapter.out.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.com.betacom.itemsapp.application.port.out.DateTimeProvider;
import pl.com.betacom.itemsapp.application.port.out.TokenProvider;

@Component
@RequiredArgsConstructor
@Slf4j
class JwtTokenProvider implements TokenProvider {

    private final JwtProperties properties;
    private final DateTimeProvider dateTimeProvider;

    @Override
    public String issue(UUID userId) {
        Instant issuedAt = dateTimeProvider.now();
        Instant expiresAt = issuedAt.plusSeconds(expirationSeconds());
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey())
                .compact();
    }

    @Override
    public long expirationSeconds() {
        return properties.getExpirationSeconds();
    }

    @Override
    public Optional<UUID> parseUserId(String token) {
        try {
            String subject = getSubject(token);
            return Optional.of(UUID.fromString(subject));
        } catch (JwtException | IllegalArgumentException exception) {
            log.warn("Rejected a JWT");
            return Optional.empty();
        }
    }

    private String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getSecret()));
    }
}
