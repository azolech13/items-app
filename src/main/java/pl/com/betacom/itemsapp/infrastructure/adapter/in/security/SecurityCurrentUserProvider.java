package pl.com.betacom.itemsapp.infrastructure.adapter.in.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pl.com.betacom.itemsapp.application.port.out.CurrentUserProvider;

import java.util.UUID;

@Component
class SecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public UUID id() {
        return (UUID) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
