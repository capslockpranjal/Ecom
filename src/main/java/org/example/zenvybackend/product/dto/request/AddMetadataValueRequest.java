package org.example.zenvybackend.product.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


import java.util.List;
import java.util.UUID;

@Data
public class AddMetadataValueRequest {

    @NotNull (message = "Field ID must not be null")
    private UUID fieldId;

    @NotEmpty(message = "Values list must not be empty")
    private List<String> values;

}
