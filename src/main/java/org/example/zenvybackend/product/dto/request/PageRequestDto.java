package org.example.zenvybackend.product.dto.request;

import lombok.Data;

@Data
public class PageRequestDto {

    private Integer page = 0;
    private Integer size = 10;
    private String sort = "createdAt";
    private String order = "desc";
    private String query;
}
