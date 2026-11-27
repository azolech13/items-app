package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

record ShareItemRequest(@NotNull UUID userId, @NotNull @OneOfEnum(ShareItemRole.class) String role) {
}
