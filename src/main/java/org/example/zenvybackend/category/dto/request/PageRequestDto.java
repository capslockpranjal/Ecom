package org.example.zenvybackend.category.dto.request;

import lombok.Data;

@Data
public class PageRequestDto {

    private Integer max = 10;

    private Integer offset = 0;

    private String sort;

    private String order;

    private String query;

}
