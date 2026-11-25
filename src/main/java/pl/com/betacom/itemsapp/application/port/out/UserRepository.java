package pl.com.betacom.itemsapp.application.port.out;

import java.util.Optional;
import pl.com.betacom.itemsapp.domain.User;

public interface UserRepository {

    boolean existsByLogin(String login);

    Optional<User> findByLogin(String login);

    User save(User user);
}
