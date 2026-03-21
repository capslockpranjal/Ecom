package org.example.zenvybackend.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCategoryRequest {

    @NotBlank(message = "Category name must not be blank")
    private String name;

}
