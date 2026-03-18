package org.example.zenvybackend.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddMetadataFieldRequest {

    @NotBlank(message = "Metadata field name must not be blank")
    private String name;

}
