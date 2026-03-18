package org.example.zenvybackend.product.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class MetadataFieldResponse {

    private UUID fieldId;

    private String name;

    private List<String> values;

}
