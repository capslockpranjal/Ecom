package org.example.zenvybackend.common.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.zenvybackend.product.dto.response.CustomerProductListItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProductListPage implements Serializable {

    private List<CustomerProductListItemResponse> content = new ArrayList<>();
    private long totalElements;
    private int number;
    private int size;

    public static CustomerProductListPage from(Page<CustomerProductListItemResponse> page) {
        return new CustomerProductListPage(
                new ArrayList<>(page.getContent()),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize()
        );
    }

    public Page<CustomerProductListItemResponse> toPage() {
        int pageSize = size > 0 ? size : Math.max(content.size(), 1);
        return new PageImpl<>(content, PageRequest.of(number, pageSize), totalElements);
    }
}
