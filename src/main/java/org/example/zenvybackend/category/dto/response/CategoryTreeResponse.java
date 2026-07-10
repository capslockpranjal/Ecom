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
public class CategoryTreeResponse {

    private UUID id;

    private String name;

    private List<ParentCategoryResponse> parentChain;

    private List<ChildCategoryResponse> children;

    private List<MetadataFieldResponse> metadataFields;

}
