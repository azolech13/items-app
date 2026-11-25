package pl.com.betacom.itemsapp.application.port.out;

import java.util.Optional;
import java.util.UUID;

import lombok.NonNull;
import pl.com.betacom.itemsapp.domain.User;

public interface UserRepository {

    @NonNull User findById(UUID id);

    boolean existsById(UUID id);

    boolean existsByLogin(String login);

    Optional<User> findByLogin(String login);

    User save(User user);
}
