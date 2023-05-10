package com.bookshop.services;

import com.bookshop.dao.SizeProduct;
import com.bookshop.dto.SizeProductDTO;
import com.bookshop.dto.pagination.PaginateDTO;
import com.bookshop.specifications.GenericSpecification;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface SizeProductService {
    List<SizeProduct> getAllSizeByProductId(Long productId);

    Long getQuantityProductBySizeAndProductId(String size, Long productId);

    void update(SizeProduct sizeProduct);

    SizeProduct getSizeProductByProductIdAndSize(Long productId, String size);

    PaginateDTO<SizeProduct> getList(Integer page, Integer perPage, GenericSpecification<SizeProduct> specification);

    void addOrUpdateSizeProduct(SizeProductDTO sizeProductDTO);
}
