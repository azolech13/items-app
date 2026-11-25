package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    boolean existsByLogin(String login);

    Optional<UserEntity> findByLogin(String login);
}
