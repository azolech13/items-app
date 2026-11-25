package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import java.time.Instant;
import java.util.UUID;

record UpdateItemResponse(UUID id, String title, String content, Integer version, Instant updatedAt) {
}
