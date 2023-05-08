package com.bookshop.controllers;

import com.bookshop.base.BaseController;
import com.bookshop.dao.*;
import com.bookshop.dto.OrderItemDTO;
import com.bookshop.exceptions.AppException;
import com.bookshop.exceptions.NotFoundException;
import com.bookshop.services.*;
import com.bookshop.specifications.GenericSpecification;
import com.bookshop.specifications.SearchCriteria;
import com.bookshop.specifications.SearchOperation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping(value = "/api/carts")
@SecurityRequirement(name = "Authorization")
public class CartController extends BaseController<Object> {

    @Autowired
    private ProductService productService;

    @Autowired
    private SaleOrderService saleOrderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private SizeProductService sizeProductService;

    @GetMapping
    @PreAuthorize("@userAuthorizer.isMember(authentication)")
    public ResponseEntity<?> getOrderItemsOfCart(HttpServletRequest request) {
        User requestedUser = (User) request.getAttribute("user");

        Delivery delivery = deliveryService.findByAddedToCartState();

        GenericSpecification<SaleOrder> specification = new GenericSpecification<>();
        specification.add(new SearchCriteria("user", requestedUser.getId(), SearchOperation.EQUAL));
        specification.add(new SearchCriteria("delivery", delivery.getId(), SearchOperation.EQUAL));

        SaleOrder saleOrder = saleOrderService.findOne(specification);

        return this.resSuccess(saleOrder);
    }

    @PostMapping
    @PreAuthorize("@userAuthorizer.isMember(authentication)")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> addToCart(@RequestBody @Valid OrderItemDTO orderItemDTO, HttpServletRequest request) {
        User requestedUser = (User) request.getAttribute("user");

        Product product = productService.findById(orderItemDTO.getProductId());
        if (product == null) {
            throw new NotFoundException("Không tìm thấy sản phẩm");
        }
//
//        if (product.getCurrentNumber() < orderItemDTO.getQuantity()) {
//            throw new AppException("Số lượng không đủ");
//        }

        Long quantitySize = sizeProductService.getQuantityProductBySizeAndProductId(orderItemDTO.getSize(), orderItemDTO.getProductId());
        if(orderItemDTO.getQuantity() > quantitySize)
            throw new NotFoundException("Size này không đủ số lượng");

        Delivery delivery = deliveryService.findByAddedToCartState();

        GenericSpecification<SaleOrder> specification = new GenericSpecification<>();
        specification.add(new SearchCriteria("user", requestedUser.getId(), SearchOperation.EQUAL));
        specification.add(new SearchCriteria("delivery", delivery.getId(), SearchOperation.EQUAL));
        SaleOrder oldSaleOrder = saleOrderService.findOne(specification);

        // update order item if exists
        if (oldSaleOrder != null) {
            GenericSpecification<OrderItem> orderItemGenericSpecification = new GenericSpecification<>();
            orderItemGenericSpecification.add(new SearchCriteria("saleOrder", oldSaleOrder.getId(), SearchOperation.EQUAL));
            orderItemGenericSpecification.add(new SearchCriteria("product", product.getId(), SearchOperation.EQUAL));

            OrderItem oldOrderItem = orderItemService.findOne(orderItemGenericSpecification);

            OrderItem newOrderItem;

            if (oldOrderItem != null) {
                oldOrderItem.setQuantity(oldOrderItem.getQuantity() + orderItemDTO.getQuantity());
                newOrderItem = orderItemService.createOrUpdate(oldOrderItem);
            } else {
                OrderItem orderItem = new OrderItem();
                orderItem.setSaleOrder(oldSaleOrder);
                orderItem.setProduct(product);
                orderItem.setSize(oldOrderItem.getSize());
                orderItem.setQuantity(orderItemDTO.getQuantity());
                newOrderItem = orderItemService.createOrUpdate(orderItem);
            }

            return this.resSuccess(newOrderItem);
        }

        // create new sale order and order item
        SaleOrder saleOrder = new SaleOrder();
        saleOrder.setUser(requestedUser);
        saleOrder.setDelivery(delivery);
        saleOrder.setCustomerAddress(requestedUser.getAddress());
        saleOrder.setPhone(requestedUser.getPhone());

        SaleOrder newSaleOrder = saleOrderService.create(saleOrder);

        OrderItem orderItem = new OrderItem();
        orderItem.setSaleOrder(newSaleOrder);
        orderItem.setProduct(product);
        orderItem.setSize(orderItemDTO.getSize());
        orderItem.setQuantity(orderItemDTO.getQuantity());

        OrderItem newOrderItem = orderItemService.createOrUpdate(orderItem);

        return this.resSuccess(newOrderItem);
    }
}
