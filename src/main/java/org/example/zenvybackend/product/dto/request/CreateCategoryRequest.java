package org.example.zenvybackend.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateCategoryRequest {

    @NotBlank(message = "Category name must not be blank")
    private String name;

    private UUID parentId;

}
