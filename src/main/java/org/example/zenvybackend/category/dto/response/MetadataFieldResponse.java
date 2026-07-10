package org.example.zenvybackend.category.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataFieldResponse {

    private UUID fieldId;

    private String name;

    private List<String> values;

}
