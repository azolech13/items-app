package pl.com.betacom.itemsapp.application.port.out;

import java.time.Instant;

public interface DateTimeProvider {

    Instant now();
}
