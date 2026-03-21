package org.example.zenvybackend.product.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.zenvybackend.category.dto.response.ParentCategoryResponse;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CustomerProductCategoryResponse {

    private UUID id;
    private String name;
    private List<ParentCategoryResponse> parentChain;
}
