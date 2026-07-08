package org.example.zenvybackend.common.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.images")
public class ImageStorageProperties {

    private String storageType = "filesystem";

    private String basePath = "uploads";

    private String publicBaseUrl = "/files";

    private long maxFileSizeBytes = 5 * 1024 * 1024;

    private final S3 s3 = new S3();

    @Getter
    @Setter
    public static class S3 {

        private String bucket;

        private String region;

        private String publicBaseUrl;

        private String endpoint;
    }
}
