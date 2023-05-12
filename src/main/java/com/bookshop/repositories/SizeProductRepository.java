package com.bookshop.repositories;

import com.bookshop.dao.SizeProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SizeProductRepository extends JpaRepository<SizeProduct, Long>, JpaSpecificationExecutor<SizeProduct> {
    List<SizeProduct> findAllByProductId(Long productId);

    SizeProduct findByProductIdAndSize(Long productId, String size);

    List<SizeProduct> findByProductId(Long productId);

    @Query(value = "select sp.*\n" +
            "from size_products sp join products p on sp.product_id = p.id\n" +
            "join categories c on p.category_id = c.id\n" +
            "where c.slug = :slug", nativeQuery = true)
    List<SizeProduct> getAll(String slug);
}
