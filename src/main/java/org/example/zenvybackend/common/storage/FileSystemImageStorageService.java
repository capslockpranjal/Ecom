package org.example.zenvybackend.common.storage;

import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class FileSystemImageStorageService implements ImageStorageService {

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "bmp");
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/bmp"
    );

    private final Path basePath;
    private final String publicBaseUrl;
    private final long maxFileSizeBytes;

    public FileSystemImageStorageService(ImageStorageProperties properties) {
        this.basePath = Path.of(properties.getBasePath()).toAbsolutePath().normalize();
        this.publicBaseUrl = normalizeBaseUrl(properties.getPublicBaseUrl());
        this.maxFileSizeBytes = properties.getMaxFileSizeBytes();

        try {
            Files.createDirectories(basePath);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to initialize image storage", ex);
        }
    }

    @Override
    public String storeUserProfileImage(UUID userId, MultipartFile file) {
        StoredImage stored = validateAndRead(file, "Profile image");
        Path directory = userDirectory(userId);
        try {
            Files.createDirectories(directory);
            deleteByPrefix(directory, "profile.");
            moveStoredImage(stored, directory.resolve("profile." + stored.extension()));
        } catch (IOException ex) {
            cleanupQuietly(stored.tempPath());
            throw new BadRequestException("Failed to store profile image");
        }
        return buildUserProfileUrl(userId);
    }

    @Override
    public String getUserProfileImageUrl(UUID userId) {
        return findProfileImage(userId) == null ? null : buildUserProfileUrl(userId);
    }

    @Override
    public Resource loadUserProfileImage(UUID userId) {
        Path file = findProfileImage(userId);
        if (file == null) {
            throw new ResourceNotFoundException("Image not found");
        }
        return toResource(file);
    }

    @Override
    public void storeVariationPrimaryImage(UUID productId, UUID variationId, MultipartFile file) {
        StoredImage stored = validateAndRead(file, "Primary image");
        Path directory = variationDirectory(productId, variationId);
        try {
            Files.createDirectories(directory);
            deleteByPrefix(directory, "primary.");
            moveStoredImage(stored, directory.resolve("primary." + stored.extension()));
        } catch (IOException ex) {
            cleanupQuietly(stored.tempPath());
            throw new BadRequestException("Failed to store primary image");
        }
    }

    @Override
    public void replaceVariationSecondaryImages(UUID productId, UUID variationId, List<MultipartFile> files) {
        Path secondaryDirectory = variationSecondaryDirectory(productId, variationId);
        Path tempDirectory = secondaryDirectory.resolveSibling(secondaryDirectory.getFileName() + "-tmp-" + UUID.randomUUID());

        try {
            if (files == null || files.isEmpty()) {
                deleteDirectory(secondaryDirectory);
                return;
            }

            Files.createDirectories(tempDirectory);
            List<StoredImage> stagedImages = new ArrayList<>();
            try {
                for (MultipartFile file : files) {
                    stagedImages.add(validateAndRead(file, "Secondary image"));
                }

                for (int index = 0; index < stagedImages.size(); index++) {
                    StoredImage stored = stagedImages.get(index);
                    moveStoredImage(stored, tempDirectory.resolve((index + 1) + "." + stored.extension()));
                }
            } catch (RuntimeException | IOException ex) {
                stagedImages.forEach(image -> cleanupQuietly(image.tempPath()));
                throw ex;
            }

            deleteDirectory(secondaryDirectory);
            Files.createDirectories(secondaryDirectory.getParent());
            moveDirectory(tempDirectory, secondaryDirectory);
        } catch (IOException ex) {
            deleteDirectoryQuietly(tempDirectory);
            throw new BadRequestException("Failed to store secondary images");
        }
    }

    @Override
    public String getVariationPrimaryImageUrl(UUID productId, UUID variationId) {
        return findVariationPrimaryImage(productId, variationId) == null
                ? null
                : publicBaseUrl + "/products/" + productId + "/variations/" + variationId + "/primary";
    }

    @Override
    public List<String> getVariationSecondaryImageUrls(UUID productId, UUID variationId) {
        List<Path> images = listSecondaryImages(productId, variationId);
        List<String> urls = new ArrayList<>();
        for (int index = 0; index < images.size(); index++) {
            urls.add(publicBaseUrl + "/products/" + productId + "/variations/" + variationId + "/secondary/" + (index + 1));
        }
        return urls;
    }

    @Override
    public Resource loadVariationPrimaryImage(UUID productId, UUID variationId) {
        Path file = findVariationPrimaryImage(productId, variationId);
        if (file == null) {
            throw new ResourceNotFoundException("Image not found");
        }
        return toResource(file);
    }

    @Override
    public Resource loadVariationSecondaryImage(UUID productId, UUID variationId, int index) {
        if (index <= 0) {
            throw new ResourceNotFoundException("Image not found");
        }

        List<Path> images = listSecondaryImages(productId, variationId);
        if (index > images.size()) {
            throw new ResourceNotFoundException("Image not found");
        }
        return toResource(images.get(index - 1));
    }

    private StoredImage validateAndRead(MultipartFile file, String label) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(label + " is required");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), ""));
        String extension = getExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Invalid image format");
        }

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
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

            Path tempFile = Files.createTempFile(basePath, "img-", "." + extension);
            Files.write(tempFile, bytes);
            return new StoredImage(extension, tempFile);
        } catch (BadRequestException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new BadRequestException("Failed to process image");
        }
    }

    private void moveStoredImage(StoredImage stored, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        moveFile(stored.tempPath(), target);
    }

    private Path userDirectory(UUID userId) {
        return basePath.resolve("users").resolve(userId.toString());
    }

    private Path variationDirectory(UUID productId, UUID variationId) {
        return basePath.resolve("products")
                .resolve(productId.toString())
                .resolve("variations")
                .resolve(variationId.toString());
    }

    private Path variationSecondaryDirectory(UUID productId, UUID variationId) {
        return variationDirectory(productId, variationId).resolve("secondary");
    }

    private Path findProfileImage(UUID userId) {
        return findByPrefix(userDirectory(userId), "profile.");
    }

    private Path findVariationPrimaryImage(UUID productId, UUID variationId) {
        return findByPrefix(variationDirectory(productId, variationId), "primary.");
    }

    private Path findByPrefix(Path directory, String prefix) {
        if (!Files.isDirectory(directory)) {
            return null;
        }

        try (Stream<Path> files = Files.list(directory)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(prefix))
                    .sorted()
                    .findFirst()
                    .orElse(null);
        } catch (IOException ex) {
            return null;
        }
    }

    private List<Path> listSecondaryImages(UUID productId, UUID variationId) {
        Path directory = variationSecondaryDirectory(productId, variationId);
        if (!Files.isDirectory(directory)) {
            return List.of();
        }

        try (Stream<Path> files = Files.list(directory)) {
            return files
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparingInt(this::secondaryIndex).thenComparing(path -> path.getFileName().toString()))
                    .toList();
        } catch (IOException ex) {
            return List.of();
        }
    }

    private int secondaryIndex(Path path) {
        String filename = path.getFileName().toString();
        int separator = filename.indexOf('.');
        String prefix = separator >= 0 ? filename.substring(0, separator) : filename;
        try {
            return Integer.parseInt(prefix);
        } catch (NumberFormatException ex) {
            return Integer.MAX_VALUE;
        }
    }

    private void deleteByPrefix(Path directory, String prefix) throws IOException {
        if (!Files.isDirectory(directory)) {
            return;
        }
        try (Stream<Path> files = Files.list(directory)) {
            for (Path file : files.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().startsWith(prefix)).toList()) {
                Files.deleteIfExists(file);
            }
        }
    }

    private void deleteDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private void deleteDirectoryQuietly(Path directory) {
        try {
            deleteDirectory(directory);
        } catch (IOException ignored) {
        }
    }

    private void cleanupQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    private void moveDirectory(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void moveFile(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Resource toResource(Path path) {
        try {
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists()) {
                throw new ResourceNotFoundException("Image not found");
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Image not found");
        }
    }

    private String getExtension(String filename) {
        int extensionIndex = filename.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex == filename.length() - 1) {
            throw new BadRequestException("Invalid image format");
        }
        return filename.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String buildUserProfileUrl(UUID userId) {
        return publicBaseUrl + "/users/" + userId + "/profile";
    }

    private String normalizeBaseUrl(String baseUrl) {
        String normalized = (baseUrl == null || baseUrl.isBlank()) ? "/files" : baseUrl.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private record StoredImage(String extension, Path tempPath) {
    }
}
