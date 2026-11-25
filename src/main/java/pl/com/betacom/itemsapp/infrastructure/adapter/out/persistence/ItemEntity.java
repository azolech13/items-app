package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.springframework.lang.NonNull;

@Entity
@Table(name = "items")
@Audited
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
class ItemEntity {

    @Id
    @NonNull
    private UUID id;

    @NonNull
    private UUID ownerId;

    @Setter
    @NonNull
    private String title;

    @Setter
    private String content;

    @Version
    private Integer version;

    @Setter
    private boolean deleted;

    @NonNull
    private Instant createdAt;

    @Setter
    @NonNull
    private Instant updatedAt;
}
