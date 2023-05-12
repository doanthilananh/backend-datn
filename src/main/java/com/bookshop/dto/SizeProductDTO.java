package com.bookshop.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
public class SizeProductDTO {
    private Long productId;
    private List<Size> sizes = new ArrayList<>();

    @Data
    @NoArgsConstructor
    public static class Size{
        private Long quantity;
        private String size;
    }
}
