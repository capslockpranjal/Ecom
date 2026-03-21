package org.example.zenvybackend.common.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ImageStorageService {

    String storeUserProfileImage(UUID userId, MultipartFile file);

    String getUserProfileImageUrl(UUID userId);

    Resource loadUserProfileImage(UUID userId);

    void storeVariationPrimaryImage(UUID productId, UUID variationId, MultipartFile file);

    void replaceVariationSecondaryImages(UUID productId, UUID variationId, List<MultipartFile> files);

    String getVariationPrimaryImageUrl(UUID productId, UUID variationId);

    List<String> getVariationSecondaryImageUrls(UUID productId, UUID variationId);

    Resource loadVariationPrimaryImage(UUID productId, UUID variationId);

    Resource loadVariationSecondaryImage(UUID productId, UUID variationId, int index);
}
