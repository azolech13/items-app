package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence;

import java.util.UUID;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthorRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ((AuthorRevision) revisionEntity).setChangedBy(userId.toString());
    }
}
