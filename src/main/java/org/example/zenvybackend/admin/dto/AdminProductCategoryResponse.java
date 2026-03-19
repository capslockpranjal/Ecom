package org.example.zenvybackend.admin.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.zenvybackend.category.dto.response.ParentCategoryResponse;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class AdminProductCategoryResponse {

    private UUID id;
    private String name;
    private List<ParentCategoryResponse> parentChain;
}
