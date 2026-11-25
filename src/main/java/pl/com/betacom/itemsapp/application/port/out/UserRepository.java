package pl.com.betacom.itemsapp.application.port.out;

import pl.com.betacom.itemsapp.domain.User;

public interface UserRepository {

    boolean existsByLogin(String login);

    User save(User user);
}
