package org.example.zenvybackend.common.util;

import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.product.dto.request.PageRequestDto;
import org.springframework.data.domain.*;

import java.util.List;

public class PageUtils {

    public static Pageable getPageable(PageRequestDto dto, List<String> allowedSortFields) {

        String sort = (dto.getSort() == null || dto.getSort().isBlank()) ? "name" : dto.getSort();
        String order = (dto.getOrder() == null || dto.getOrder().isBlank()) ? "asc" : dto.getOrder();

        int max = (dto.getMax() == null || dto.getMax() <= 0) ? 10 : dto.getMax();
        int offset = (dto.getOffset() == null || dto.getOffset() < 0) ? 0 : dto.getOffset();

        if (!allowedSortFields.contains(sort)) {
            throw new BadRequestException("Invalid sort field");
        }

        Sort.Direction direction =
                order.equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        return PageRequest.of(offset, max, Sort.by(direction, sort));
    }
}
