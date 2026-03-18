package org.example.zenvybackend.product.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CategoryTreeResponse {

    private UUID id;

    private String name;

    private List<ParentCategoryResponse> parentChain;

    private List<ChildCategoryResponse> children;

    private List<MetadataFieldResponse> metadataFields;

}
