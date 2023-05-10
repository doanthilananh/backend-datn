package com.bookshop.dto;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class SizeProductDTO {
    private Long quantity;
    private Long productId;
    private String size;
}
