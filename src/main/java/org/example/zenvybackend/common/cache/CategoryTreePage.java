package org.example.zenvybackend.common.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.zenvybackend.category.dto.response.CategoryTreeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreePage implements Serializable {

    private List<CategoryTreeResponse> content = new ArrayList<>();
    private long totalElements;
    private int number;
    private int size;

    public static CategoryTreePage from(Page<CategoryTreeResponse> page) {
        return new CategoryTreePage(
                new ArrayList<>(page.getContent()),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize()
        );
    }

    public Page<CategoryTreeResponse> toPage() {
        int pageSize = size > 0 ? size : Math.max(content.size(), 1);
        return new PageImpl<>(content, PageRequest.of(number, pageSize), totalElements);
    }
}
