package pl.com.betacom.itemsapp.infrastructure.adapter.out.uuid;

import java.util.UUID;
import org.springframework.stereotype.Component;
import pl.com.betacom.itemsapp.application.port.out.UuidGenerator;

@Component
class RandomUuidGenerator implements UuidGenerator {

    @Override
    public UUID generate() {
        return UUID.randomUUID();
    }
}
