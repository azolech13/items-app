package pl.com.betacom.itemsapp.application.port.out;

public interface PasswordHasher {

    String hash(String rawPassword);
}
