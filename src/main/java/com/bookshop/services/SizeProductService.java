package com.bookshop.services;

import com.bookshop.dao.SizeProduct;

import java.util.List;

public interface SizeProductService {
    List<SizeProduct> getAllSizeByProductId(Long productId);

    Long getQuantityProductBySizeAndProductId(String size, Long productId);

    void update(SizeProduct sizeProduct);

    SizeProduct getSizeProductByProductIdAndSize(Long productId, String size);
}
