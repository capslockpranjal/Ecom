package org.example.zenvybackend.product.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateCategoryRequest {

    private String name;

    private UUID parentId;

}
