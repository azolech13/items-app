package pl.com.betacom.itemsapp.infrastructure.adapter.out.time;

import java.time.Instant;
import org.springframework.stereotype.Component;
import pl.com.betacom.itemsapp.application.port.out.DateTimeProvider;

@Component
class SystemDateTimeProvider implements DateTimeProvider {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
