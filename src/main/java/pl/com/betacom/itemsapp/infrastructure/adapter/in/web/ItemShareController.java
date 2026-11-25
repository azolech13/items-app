package pl.com.betacom.itemsapp.infrastructure.adapter.in.web;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.com.betacom.itemsapp.application.port.in.DeleteItemShare;
import pl.com.betacom.itemsapp.application.port.in.DeleteItemShareCommand;
import pl.com.betacom.itemsapp.application.port.in.ShareItem;
import pl.com.betacom.itemsapp.application.port.in.ShareItemCommand;
import pl.com.betacom.itemsapp.application.port.in.ShareResult;
import pl.com.betacom.itemsapp.domain.ItemRole;

@RestController
@RequestMapping("/items/{id}/share")
@RequiredArgsConstructor
class ItemShareController {

    private final ShareItem shareItem;
    private final DeleteItemShare deleteItemShare;

    @PostMapping
    ResponseEntity<ShareItemResponse> share(@PathVariable UUID id, @Valid @RequestBody ShareItemRequest request) {
        ShareResult result = shareItem.share(new ShareItemCommand(id, request.userId(), ItemRole.valueOf(request.role().name())));
        ShareItemResponse body = new ShareItemResponse(result.itemId(), result.userId(), result.role(), result.grantedAt());
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(body);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID id, @PathVariable UUID userId) {
        deleteItemShare.delete(new DeleteItemShareCommand(id, userId));
    }
}
