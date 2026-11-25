package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence

import org.springframework.dao.DataIntegrityViolationException
import pl.com.betacom.itemsapp.domain.LoginTakenException
import spock.lang.Specification

import static pl.com.betacom.itemsapp.UserTestConstants.user

class UserRepositoryAdapterSpec extends Specification {

    UserJpaRepository users = Mock()
    UserRepositoryAdapter adapter = new UserRepositoryAdapter(users)

    def "translates a unique login violation into a domain exception"() {
        when:
        adapter.save(user())

        then:
        1 * users.saveAndFlush(_) >> { throw new DataIntegrityViolationException("uk_users_login") }
        thrown(LoginTakenException)
    }
}
