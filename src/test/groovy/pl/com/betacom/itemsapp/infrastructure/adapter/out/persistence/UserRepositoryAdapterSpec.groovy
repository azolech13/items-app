package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence

import org.springframework.dao.DataIntegrityViolationException
import pl.com.betacom.itemsapp.domain.LoginTakenException
import spock.lang.Specification

import static pl.com.betacom.itemsapp.UserTestConstants.CREATED_AT
import static pl.com.betacom.itemsapp.UserTestConstants.ID
import static pl.com.betacom.itemsapp.UserTestConstants.LOGIN
import static pl.com.betacom.itemsapp.UserTestConstants.PASSWORD_HASH
import static pl.com.betacom.itemsapp.UserTestConstants.user

class UserRepositoryAdapterSpec extends Specification {

    UserJpaRepository users = Mock()
    UserRepositoryAdapter adapter = new UserRepositoryAdapter(users)

    def "maps a user found by id"() {
        when:
        def found = adapter.findById(ID)

        then:
        1 * users.findById(ID) >> Optional.of(new UserEntity(ID, LOGIN, PASSWORD_HASH, CREATED_AT))
        found == user()
    }

    def "fails when the user does not exist"() {
        when:
        adapter.findById(ID)

        then:
        1 * users.findById(ID) >> Optional.empty()
        def missing = thrown(IllegalStateException)
        missing.message == String.format(UserRepositoryAdapter.NOT_FOUND, ID)
    }

    def "translates a unique login violation into a domain exception"() {
        when:
        adapter.save(user())

        then:
        1 * users.saveAndFlush(_) >> { throw new DataIntegrityViolationException("uk_users_login") }
        thrown(LoginTakenException)
    }
}
