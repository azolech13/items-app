package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import pl.com.betacom.itemsapp.application.port.out.UserRepository;
import pl.com.betacom.itemsapp.domain.LoginTakenException;
import pl.com.betacom.itemsapp.domain.User;

@Component
@RequiredArgsConstructor
@Slf4j
class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository users;

    @Override
    public boolean existsByLogin(String login) {
        return users.existsByLogin(login);
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return users.findByLogin(login)
                .map(UserRepositoryAdapter::toDomain);
    }

    @Override
    public User save(User user) {
        try {
            users.saveAndFlush(toEntity(user));
            return user;
        } catch (DataIntegrityViolationException exception) {
            log.warn("Registration hit unique login constraint for {}", user.login());
            throw new LoginTakenException(user.login());
        }
    }

    private static UserEntity toEntity(User user) {
        return new UserEntity(user.id(), user.login(), user.passwordHash(), user.createdAt());
    }

    private static User toDomain(UserEntity entity) {
        return new User(entity.getId(), entity.getLogin(), entity.getPassword(), entity.getCreatedAt());
    }
}
