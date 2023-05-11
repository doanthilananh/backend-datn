package com.bookshop.controllers;

import com.bookshop.base.BaseController;
import com.bookshop.dao.OrderItem;
import com.bookshop.dao.Product;
import com.bookshop.dao.SaleOrder;
import com.bookshop.dao.SizeProduct;
import com.bookshop.dto.OrderItemUpdateDTO;
import com.bookshop.exceptions.AppException;
import com.bookshop.exceptions.NotFoundException;
import com.bookshop.services.OrderItemService;
import com.bookshop.services.ProductService;
import com.bookshop.services.SaleOrderService;
import com.bookshop.services.SizeProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/order-items")
@SecurityRequirement(name = "Authorization")
public class OrderItemController extends BaseController<OrderItem> {

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SaleOrderService saleOrderService;
    @Autowired
    private SizeProductService sizeProductService;
    @PatchMapping("/{orderItemId}")
    @PreAuthorize("@userAuthorizer.isMember(authentication)")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> updateQuantity(@PathVariable("orderItemId") Long orderItemId,
                                            @RequestBody @Valid OrderItemUpdateDTO orderItemUpdateDTO) {
        OrderItem orderItem = orderItemService.findById(orderItemId);
        if (orderItem == null) {
            throw new NotFoundException("Giỏ hàng trống");
        }

        Product product = orderItem.getProduct();
        Long quantitySize = sizeProductService.getQuantityProductBySizeAndProductId( orderItem.getSize(), product.getId());

//        int currentNumber = product.getCurrentNumber() + orderItem.getQuantity();
        if (quantitySize < orderItemUpdateDTO.getQuantity()) {
            throw new AppException("Số lượng không đủ");
        }

        Integer updatedCurrentNumber = Math.toIntExact((quantitySize + orderItem.getQuantity()) - orderItemUpdateDTO.getQuantity());

        orderItem.setQuantity(orderItemUpdateDTO.getQuantity());
        orderItemService.createOrUpdate(orderItem);

        SizeProduct sizeProduct = sizeProductService.getSizeProductByProductIdAndSize(product.getId(), orderItem.getSize());
        sizeProduct.setQuantity(Long.valueOf(updatedCurrentNumber));
        sizeProductService.update(sizeProduct);
//        productService.update(product);

        return this.resSuccess(orderItem);
    }

    @DeleteMapping("/{orderItemId}")
    @PreAuthorize("@userAuthorizer.isMember(authentication)")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> deleteOrderItem(@PathVariable("orderItemId") Long orderItemId) {
        OrderItem orderItem = orderItemService.findById(orderItemId);
        if (orderItem == null) {
            throw new NotFoundException("Giỏ hàng trống");
        }

        orderItemService.deleteById(orderItemId);

        SaleOrder saleOrder = saleOrderService.findById(orderItem.getSaleOrder().getId());
        if (saleOrder.getOrderItems().size() == 0) {
            saleOrderService.deleteById(saleOrder.getId());
        }

        return this.resSuccess(orderItem);
    }
}
