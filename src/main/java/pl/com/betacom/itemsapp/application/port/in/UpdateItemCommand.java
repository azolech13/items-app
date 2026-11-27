package pl.com.betacom.itemsapp.application.port.in;

import java.util.UUID;

public record UpdateItemCommand(
        UUID id, String title, String content, boolean contentSpecified, Integer version) {
}
