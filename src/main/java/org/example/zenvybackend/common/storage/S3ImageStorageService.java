package org.example.zenvybackend.common.storage;

import lombok.extern.slf4j.Slf4j;
import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.MetadataDirective;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.images.storage-type", havingValue = "s3")
public class S3ImageStorageService implements ImageStorageService {

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "bmp");
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/bmp"
    );

    private final S3Client s3Client;
    private final String bucket;
    private final String publicBaseUrl;
    private final long maxFileSizeBytes;

    public S3ImageStorageService(ImageStorageProperties properties) {
        ImageStorageProperties.S3 s3 = properties.getS3();
        if (!StringUtils.hasText(s3.getBucket())) {
            throw new IllegalStateException("app.images.s3.bucket is required when S3 storage is enabled");
        }
        if (!StringUtils.hasText(s3.getRegion())) {
            throw new IllegalStateException("app.images.s3.region is required when S3 storage is enabled");
        }

        this.bucket = s3.getBucket().trim();
        this.publicBaseUrl = resolvePublicBaseUrl(s3);
        this.maxFileSizeBytes = properties.getMaxFileSizeBytes();

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(s3.getRegion().trim()));
        if (StringUtils.hasText(s3.getEndpoint())) {
            builder.endpointOverride(URI.create(s3.getEndpoint().trim()));
        }
        this.s3Client = builder.build();
    }

    @Override
    public String storeUserProfileImage(UUID userId, MultipartFile file) {
        ValidatedImage image = validate(file, "Profile image");
        String prefix = userPrefix(userId);
        String key = prefix + "profile." + image.extension();
        deleteByPrefix(prefix + "profile.");
        putObject(key, image);
        log.info("Profile image stored in S3: userId={}", userId);
        return buildPublicUrl(key);
    }

    @Override
    public String getUserProfileImageUrl(UUID userId) {
        String key = findFirstKey(userPrefix(userId) + "profile.");
        return key == null ? null : buildPublicUrl(key);
    }

    @Override
    public Resource loadUserProfileImage(UUID userId) {
        String key = findFirstKey(userPrefix(userId) + "profile.");
        if (key == null) {
            throw new ResourceNotFoundException("Image not found");
        }
        return loadObject(key);
    }

    @Override
    public void storeVariationPrimaryImage(UUID productId, UUID variationId, MultipartFile file) {
        ValidatedImage image = validate(file, "Primary image");
        String prefix = variationPrefix(productId, variationId);
        deleteByPrefix(prefix + "primary.");
        putObject(prefix + "primary." + image.extension(), image);
        log.info("Variation primary image stored in S3: productId={}, variationId={}", productId, variationId);
    }

    @Override
    public void replaceVariationSecondaryImages(UUID productId, UUID variationId, List<MultipartFile> files) {
        String targetPrefix = secondaryPrefix(productId, variationId);
        if (files == null || files.isEmpty()) {
            deleteByPrefix(targetPrefix);
            log.info("Variation secondary images cleared in S3: productId={}, variationId={}", productId, variationId);
            return;
        }

        List<ValidatedImage> validatedImages = new ArrayList<>();
        for (MultipartFile file : files) {
            validatedImages.add(validate(file, "Secondary image"));
        }

        String stagingPrefix = variationPrefix(productId, variationId) + "secondary-staging/" + UUID.randomUUID() + "/";
        try {
            for (int index = 0; index < validatedImages.size(); index++) {
                ValidatedImage image = validatedImages.get(index);
                putObject(stagingPrefix + (index + 1) + "." + image.extension(), image);
            }

            deleteByPrefix(targetPrefix);
            for (S3ObjectRef stagedObject : listObjects(stagingPrefix)) {
                String filename = stagedObject.filename();
                copyObject(stagedObject.key(), targetPrefix + filename);
            }
        } catch (RuntimeException ex) {
            log.error("Failed to replace secondary images in S3: productId={}, variationId={}", productId, variationId, ex);
            throw ex;
        } finally {
            deleteByPrefix(stagingPrefix);
        }

        log.info("Variation secondary images replaced in S3: productId={}, variationId={}, count={}", productId, variationId, files.size());
    }

    @Override
    public String getVariationPrimaryImageUrl(UUID productId, UUID variationId) {
        String key = findFirstKey(variationPrefix(productId, variationId) + "primary.");
        return key == null ? null : buildPublicUrl(key);
    }

    @Override
    public List<String> getVariationSecondaryImageUrls(UUID productId, UUID variationId) {
        List<S3ObjectRef> objects = listObjects(secondaryPrefix(productId, variationId));
        List<String> urls = new ArrayList<>();
        for (S3ObjectRef object : objects) {
            urls.add(buildPublicUrl(object.key()));
        }
        return urls;
    }

    @Override
    public Resource loadVariationPrimaryImage(UUID productId, UUID variationId) {
        String key = findFirstKey(variationPrefix(productId, variationId) + "primary.");
        if (key == null) {
            throw new ResourceNotFoundException("Image not found");
        }
        return loadObject(key);
    }

    @Override
    public Resource loadVariationSecondaryImage(UUID productId, UUID variationId, int index) {
        if (index <= 0) {
            throw new ResourceNotFoundException("Image not found");
        }

        List<S3ObjectRef> objects = listObjects(secondaryPrefix(productId, variationId));
        if (index > objects.size()) {
            throw new ResourceNotFoundException("Image not found");
        }
        return loadObject(objects.get(index - 1).key());
    }

    private Resource loadObject(String key) {
        try {
            ResponseBytes<?> bytes = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            return new NamedByteArrayResource(bytes.asByteArray(), keyFilename(key));
        } catch (NoSuchKeyException ex) {
            throw new ResourceNotFoundException("Image not found");
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                throw new ResourceNotFoundException("Image not found");
            }
            throw new BadRequestException("Failed to load image");
        }
    }

    private void putObject(String key, ValidatedImage image) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(image.contentType())
                            .cacheControl("public, max-age=3600")
                            .build(),
                    software.amazon.awssdk.core.sync.RequestBody.fromBytes(image.bytes())
            );
        } catch (S3Exception ex) {
            throw new BadRequestException("Failed to store image");
        }
    }

    private void copyObject(String sourceKey, String targetKey) {
        try {
            s3Client.copyObject(CopyObjectRequest.builder()
                    .sourceBucket(bucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucket)
                    .destinationKey(targetKey)
                    .cacheControl("public, max-age=3600")
                    .metadataDirective(MetadataDirective.REPLACE)
                    .build());
        } catch (S3Exception ex) {
            throw new BadRequestException("Failed to store image");
        }
    }

    private void deleteByPrefix(String prefix) {
        for (S3ObjectRef object : listObjects(prefix)) {
            try {
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(object.key())
                        .build());
            } catch (S3Exception ex) {
                throw new BadRequestException("Failed to store image");
            }
        }
    }

    private String findFirstKey(String prefix) {
        List<S3ObjectRef> objects = listObjects(prefix);
        return objects.isEmpty() ? null : objects.get(0).key();
    }

    private List<S3ObjectRef> listObjects(String prefix) {
        try {
            ListObjectsV2Response response = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .build());
            return response.contents().stream()
                    .filter(object -> object.key() != null && !object.key().endsWith("/"))
                    .map(object -> new S3ObjectRef(object.key(), keyFilename(object.key())))
                    .sorted(Comparator.comparingInt(this::secondaryIndex).thenComparing(S3ObjectRef::key))
                    .toList();
        } catch (S3Exception ex) {
            throw new BadRequestException("Failed to load image");
        }
    }

    private int secondaryIndex(S3ObjectRef object) {
        String filename = object.filename();
        int separator = filename.indexOf('.');
        String prefix = separator >= 0 ? filename.substring(0, separator) : filename;
        try {
            return Integer.parseInt(prefix);
        } catch (NumberFormatException ex) {
            return Integer.MAX_VALUE;
        }
    }

    private ValidatedImage validate(MultipartFile file, String label) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(label + " is required");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), ""));
        String extension = getExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Invalid image format");
        }

        String providedContentType = file.getContentType();
        if (providedContentType != null && !ALLOWED_CONTENT_TYPES.contains(providedContentType.toLowerCase(Locale.ROOT))) {
            throw new BadRequestException("Invalid image content type");
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new BadRequestException("Image size exceeds allowed limit");
        }

        try {
            byte[] bytes = file.getBytes();
            if (ImageIO.read(new ByteArrayInputStream(bytes)) == null) {
                throw new BadRequestException("Invalid image file");
            }
            return new ValidatedImage(extension, resolveContentType(providedContentType, extension), bytes);
        } catch (BadRequestException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new BadRequestException("Failed to process image");
        }
    }

    private String resolveContentType(String providedContentType, String extension) {
        if (StringUtils.hasText(providedContentType)) {
            return providedContentType.toLowerCase(Locale.ROOT);
        }
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "bmp" -> "image/bmp";
            default -> "application/octet-stream";
        };
    }

    private String getExtension(String filename) {
        int extensionIndex = filename.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex == filename.length() - 1) {
            throw new BadRequestException("Invalid image format");
        }
        return filename.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String buildPublicUrl(String key) {
        return publicBaseUrl + "/" + key;
    }

    private String userPrefix(UUID userId) {
        return "users/" + userId + "/";
    }

    private String variationPrefix(UUID productId, UUID variationId) {
        return "products/" + productId + "/variations/" + variationId + "/";
    }

    private String secondaryPrefix(UUID productId, UUID variationId) {
        return variationPrefix(productId, variationId) + "secondary/";
    }

    private String resolvePublicBaseUrl(ImageStorageProperties.S3 s3) {
        if (StringUtils.hasText(s3.getPublicBaseUrl())) {
            return trimTrailingSlash(s3.getPublicBaseUrl().trim());
        }
        String region = s3.getRegion().trim();
        return "https://" + bucket + ".s3." + region + ".amazonaws.com";
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String keyFilename(String key) {
        int slash = key.lastIndexOf('/');
        return slash >= 0 ? key.substring(slash + 1) : key;
    }

    private record ValidatedImage(String extension, String contentType, byte[] bytes) {
    }

    private record S3ObjectRef(String key, String filename) {
    }

    private static final class NamedByteArrayResource extends ByteArrayResource {

        private final String filename;

        private NamedByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
