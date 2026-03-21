package org.example.zenvybackend.common.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class ImageController {

    private final ImageStorageService imageStorageService;

    @GetMapping("/users/{userId}/profile")
    public ResponseEntity<Resource> getUserProfileImage(@PathVariable UUID userId) {
        return toResponse(imageStorageService.loadUserProfileImage(userId));
    }

    @GetMapping("/products/{productId}/variations/{variationId}/primary")
    public ResponseEntity<Resource> getVariationPrimaryImage(
            @PathVariable UUID productId,
            @PathVariable UUID variationId
    ) {
        return toResponse(imageStorageService.loadVariationPrimaryImage(productId, variationId));
    }

    @GetMapping("/products/{productId}/variations/{variationId}/secondary/{index}")
    public ResponseEntity<Resource> getVariationSecondaryImage(
            @PathVariable UUID productId,
            @PathVariable UUID variationId,
            @PathVariable int index
    ) {
        return toResponse(imageStorageService.loadVariationSecondaryImage(productId, variationId, index));
    }

    private ResponseEntity<Resource> toResponse(Resource resource) {
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            String contentType = Files.probeContentType(Path.of(resource.getURI()));
            if (contentType != null) {
                mediaType = MediaType.parseMediaType(contentType);
            }
        } catch (IOException ignored) {
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                .body(resource);
    }
}
