package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

@Entity
@RevisionEntity(AuthorRevisionListener.class)
@Getter
@Setter
public class AuthorRevision extends DefaultRevisionEntity {

    private String changedBy;
}
