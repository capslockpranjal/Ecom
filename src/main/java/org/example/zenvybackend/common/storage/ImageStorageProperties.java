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

    private String basePath = "uploads";

    private String publicBaseUrl = "/files";

    private long maxFileSizeBytes = 5 * 1024 * 1024;
}
