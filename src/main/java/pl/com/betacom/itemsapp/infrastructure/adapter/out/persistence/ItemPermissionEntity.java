package pl.com.betacom.itemsapp.infrastructure.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import pl.com.betacom.itemsapp.domain.ItemRole;

@Entity
@Table(name = "item_permissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
class ItemPermissionEntity {

    @Id
    @NonNull
    private UUID id;

    @NonNull
    private UUID itemId;

    @NonNull
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @NonNull
    private ItemRole role;
}
