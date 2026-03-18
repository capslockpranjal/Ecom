package org.example.zenvybackend.product.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ParentCategoryResponse {

    private UUID id;

    private String name;

}
