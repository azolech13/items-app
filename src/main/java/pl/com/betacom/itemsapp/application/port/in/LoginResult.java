package pl.com.betacom.itemsapp.application.port.in;

public record LoginResult(String token, long expiresIn) {
}
