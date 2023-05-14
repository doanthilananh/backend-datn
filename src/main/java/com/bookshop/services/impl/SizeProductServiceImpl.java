package com.bookshop.services.impl;

import com.bookshop.base.BasePagination;
import com.bookshop.dao.Product;
import com.bookshop.dao.SizeProduct;
import com.bookshop.dto.SizeProductDTO;
import com.bookshop.dto.pagination.PaginateDTO;
import com.bookshop.exceptions.NotFoundException;
import com.bookshop.repositories.ProductRepository;
import com.bookshop.repositories.SizeProductRepository;
import com.bookshop.services.SizeProductService;
import com.bookshop.specifications.GenericSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SizeProductServiceImpl extends BasePagination<SizeProduct, SizeProductRepository> implements SizeProductService {
    @Autowired
    private SizeProductRepository sizeProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    public SizeProductServiceImpl(SizeProductRepository sizeProductRepository) {
        super(sizeProductRepository);
    }


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
            throw new NotFoundException("Size sản phẩm không có sẵn");
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

    @Override
    public PaginateDTO<SizeProduct> getList(Integer page, Integer perPage, GenericSpecification<SizeProduct> specification) {
        return this.paginate(page, perPage, specification);
    }

    @Override
    public List<SizeProduct> updateSizeProducts(SizeProductDTO sizeProductDTO) {
        List<SizeProduct> sizeProducts = sizeProductRepository.findAllByProductId(sizeProductDTO.getProductId());
        if(sizeProducts.isEmpty())
            throw new NotFoundException("Sản phẩm này chưa nhập kho");
        Map<String, SizeProduct> sizeProductMap = sizeProducts.stream().collect(Collectors.toMap(SizeProduct::getSize, Function.identity()));
        sizeProductDTO.getSizes().forEach(size -> {
            if(sizeProductMap.containsKey(size.getSize())){
                sizeProductMap.get(size.getSize()).setQuantity(size.getQuantity());
                sizeProductRepository.save(sizeProductMap.get(size.getSize()));
            }
        });
        return sizeProductRepository.findAllByProductId(sizeProductDTO.getProductId());
    }
}
