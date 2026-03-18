package org.example.zenvybackend.product.dto.request;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AddMetadataValueRequest {

    private UUID fieldId;

    private List<String> values;

}
