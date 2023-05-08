package com.bookshop.services.impl;

import com.bookshop.dao.Product;
import com.bookshop.dao.SizeProduct;
import com.bookshop.exceptions.NotFoundException;
import com.bookshop.repositories.ProductRepository;
import com.bookshop.repositories.SizeProductRepository;
import com.bookshop.services.SizeProductService;
import org.hibernate.engine.jdbc.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SizeProductServiceImpl implements SizeProductService {
    @Autowired
    private SizeProductRepository sizeProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<SizeProduct> getAllSizeByProductId(Long productId) {
        Optional<Product> product = productRepository.findById(productId);
        if(product.isEmpty())
            throw new NotFoundException("Sản phẩm này không tồn tại");

        List<SizeProduct> sizes = sizeProductRepository.findAllByProductId(productId);
        if(sizes.isEmpty())
            throw new NotFoundException("Sản phẩm này đã hết");

        return sizeProductRepository.findAllByProductId(productId);
    }

    @Override
    public Long getQuantityProductBySizeAndProductId(String size, Long productId) {
        SizeProduct sizeProduct = sizeProductRepository.findByProductIdAndSize(productId, size);
        if(sizeProduct == null)
            throw new NotFoundException("Sản phẩm này không có kích thước " + size);
        if(sizeProduct.getQuantity() == 0)
            throw new NotFoundException("Size " + size + "của sản phẩm này đã hết");
        return sizeProduct.getQuantity();
    }

    @Override
    public void update(SizeProduct sizeProduct) {
        sizeProductRepository.save(sizeProduct);
    }

    @Override
    public SizeProduct getSizeProductByProductIdAndSize(Long productId, String size) {
        SizeProduct sizeProduct = sizeProductRepository.findByProductIdAndSize(productId,size);
        if(sizeProduct == null)
            throw new NotFoundException("Size sản phẩm này không tồn tại");
        return sizeProduct;
    }
}
