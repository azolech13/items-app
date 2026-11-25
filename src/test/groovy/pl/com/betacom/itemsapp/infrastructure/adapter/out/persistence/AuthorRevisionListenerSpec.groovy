package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification

import static pl.com.betacom.itemsapp.UserTestConstants.ID

class AuthorRevisionListenerSpec extends Specification {

    AuthorRevisionListener listener = new AuthorRevisionListener()

    def cleanup() {
        SecurityContextHolder.clearContext()
    }

    def "stores the current user id as changedBy"() {
        given:
        SecurityContextHolder.context.authentication =
                new UsernamePasswordAuthenticationToken(ID, null, List.of())
        def revision = new AuthorRevision()

        when:
        listener.newRevision(revision)

        then:
        revision.changedBy == ID.toString()
    }
}
