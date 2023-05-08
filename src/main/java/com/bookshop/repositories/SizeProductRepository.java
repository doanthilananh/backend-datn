package com.bookshop.repositories;

import com.bookshop.dao.SizeProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SizeProductRepository extends JpaRepository<SizeProduct, Long>{
    List<SizeProduct> findAllByProductId(Long productId);

    SizeProduct findByProductIdAndSize(Long productId, String size);
}
