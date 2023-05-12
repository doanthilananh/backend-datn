package com.bookshop.controllers;

import com.bookshop.base.BaseController;
import com.bookshop.constants.Common;
import com.bookshop.dao.*;
import com.bookshop.dto.DasboardDTO;
import com.bookshop.dto.DeliveryDTO;
import com.bookshop.dto.pagination.PaginateDTO;
import com.bookshop.exceptions.AppException;
import com.bookshop.exceptions.NotFoundException;
import com.bookshop.services.*;
import com.bookshop.specifications.GenericSpecification;
import com.bookshop.specifications.SearchCriteria;
import com.bookshop.specifications.SearchOperation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping(value = "/api/sale-orders")
@SecurityRequirement(name = "Authorization")
public class SaleOrderController extends BaseController<SaleOrder> {

    @Autowired
    private SaleOrderService saleOrderService;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private SizeProductService sizeProductService;
    @GetMapping
    @PreAuthorize("@userAuthorizer.isMember(authentication)")
    public ResponseEntity<?> getListSaleOrdersForMember(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "perPage", required = false) Integer perPage,
            @RequestParam(name = "deliveryIndex", required = false) String deliveryIndex,
            HttpServletRequest request) {
        User requestedUser = (User) request.getAttribute("user");

        Delivery deliveryAddedToCart = deliveryService.findByAddedToCartState();

        GenericSpecification<SaleOrder> specification = new GenericSpecification<SaleOrder>().getBasicQuery(request);
        specification.add(new SearchCriteria("delivery", deliveryAddedToCart.getId(), SearchOperation.NOT_EQUAL));
        specification.add(new SearchCriteria("user", requestedUser.getId(), SearchOperation.EQUAL));

        if (deliveryIndex != null) {
            Delivery deliverySearch = deliveryService.findByIndex(deliveryIndex);
            if (deliverySearch != null) {
                specification.add(new SearchCriteria("delivery", deliverySearch.getId(), SearchOperation.EQUAL));
            }
        }

        PaginateDTO<SaleOrder> paginateSaleOrders = saleOrderService.getList(page, perPage, specification);

        return this.resPagination(paginateSaleOrders);
    }

    @GetMapping("/admin")
    @PreAuthorize("@userAuthorizer.isAdmin(authentication)")
    public ResponseEntity<?> getListSaleOrdersForAdmin(@RequestParam(name = "page", required = false) Integer page,
                                                       @RequestParam(name = "perPage", required = false) Integer perPage,
                                                       @RequestParam(name = "fetchType", required = false) Integer fetchType,
                                                       @RequestParam(name = "fromDate", required = false) String fromDate,
                                                       @RequestParam(name = "toDate", required = false) String toDate,
                                                       HttpServletRequest request) {
        Delivery deliveryAddedToCart = deliveryService.findByAddedToCartState();

        GenericSpecification<SaleOrder> specification = new GenericSpecification<SaleOrder>().getBasicQuery(request);
        specification.add(new SearchCriteria("delivery", deliveryAddedToCart.getId(), SearchOperation.NOT_EQUAL));

        if (fetchType != null && fetchType.equals(Common.FETCH_TYPE_ADMIN)) {
            Delivery deliveryDelivered = deliveryService.findByDeliveredState();
            specification.add(new SearchCriteria("delivery", deliveryDelivered.getId(), SearchOperation.EQUAL));
        }

        if (fromDate != null && toDate == null) {
            specification.add(new SearchCriteria("orderedAt", fromDate, SearchOperation.FROM_DATE));
        } else if (fromDate == null && toDate != null) {
            specification.add(new SearchCriteria("orderedAt", toDate, SearchOperation.TO_DATE));
        } else if (fromDate != null) {
            specification.add(new SearchCriteria("orderedAt", fromDate, SearchOperation.FROM_DATE));
            specification.add(new SearchCriteria("orderedAt", toDate, SearchOperation.TO_DATE));
        }

        PaginateDTO<SaleOrder> paginateSaleOrders = saleOrderService.getList(page, perPage, specification);

        return this.resPagination(paginateSaleOrders);
    }

    @GetMapping("/{saleOrderId}")
    @PreAuthorize("@userAuthorizer.isMember(authentication)")
    public ResponseEntity<?> getSaleOrderByIdForMember(@PathVariable("saleOrderId") Long saleOrderId, HttpServletRequest request) {
        User requestedUser = (User) request.getAttribute("user");

        GenericSpecification<SaleOrder> specification = new GenericSpecification<SaleOrder>().getBasicQuery(request);
        specification.add(new SearchCriteria("user", requestedUser.getId(), SearchOperation.EQUAL));
        specification.add(new SearchCriteria("id", saleOrderId, SearchOperation.EQUAL));

        SaleOrder saleOrder = saleOrderService.findOne(specification);

        return this.resSuccess(saleOrder);
    }

    @GetMapping("/admin/{saleOrderId}")
    @PreAuthorize("@userAuthorizer.isAdmin(authentication)")
    public ResponseEntity<?> getSaleOrderByIdForAdmin(@PathVariable("saleOrderId") Long saleOrderId) {
        SaleOrder saleOrder = saleOrderService.findById(saleOrderId);

        return this.resSuccess(saleOrder);
    }

    @PatchMapping("/{saleOrderId}")
    @PreAuthorize("@userAuthorizer.isAdmin(authentication)")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> updateSaleOrderDelivery(@PathVariable("saleOrderId") Long saleOrderId,
                                                     @RequestBody @Valid DeliveryDTO deliveryDTO) {
        Delivery deliveryAddedToCart = deliveryService.findByAddedToCartState();
        Delivery deliveryCancel = deliveryService.findByCancelState();

        GenericSpecification<SaleOrder> specification = new GenericSpecification<>();
        specification.add(new SearchCriteria("delivery", deliveryAddedToCart.getId(), SearchOperation.NOT_EQUAL));
        specification.add(new SearchCriteria("delivery", deliveryCancel.getId(), SearchOperation.NOT_EQUAL));
        specification.add(new SearchCriteria("id", saleOrderId, SearchOperation.EQUAL));

        SaleOrder saleOrder = saleOrderService.findOne(specification);

        if (saleOrder == null) {
            throw new NotFoundException("Chi tiết đơn hàng không có sẵn");
        }

        Delivery delivery = deliveryService.findById(deliveryDTO.getDeliveryId());

        if (delivery == null) {
            throw new NotFoundException("Not found delivery");
        }

        saleOrder.setDelivery(delivery);
        SaleOrder savedSaleOrder = saleOrderService.update(saleOrder);

        if (delivery.getIndex().equals(Common.DELIVERY_CANCELED_INDEX)) {
            List<OrderItem> orderItems = saleOrder.getOrderItems();

            for (OrderItem orderItem : orderItems) {
                Product product = orderItem.getProduct();
                product.setCurrentNumber(product.getCurrentNumber() + orderItem.getQuantity());
                productService.update(product);
            }
        }

        return this.resSuccess(savedSaleOrder);
    }

    @PatchMapping("/{saleOrderId}/payment")
    @PreAuthorize("@userAuthorizer.isMember(authentication)")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> handlePaymentSaleOrder(@PathVariable("saleOrderId") Long saleOrderId,
                                                    HttpServletRequest request) {
        User requestedUser = (User) request.getAttribute("user");

        Delivery delivery = deliveryService.findByAddedToCartState();

        GenericSpecification<SaleOrder> specification = new GenericSpecification<>();
        specification.add(new SearchCriteria("user", requestedUser.getId(), SearchOperation.EQUAL));
        specification.add(new SearchCriteria("delivery", delivery.getId(), SearchOperation.EQUAL));
        specification.add(new SearchCriteria("id", saleOrderId, SearchOperation.EQUAL));

        SaleOrder saleOrder = saleOrderService.findOne(specification);

        if (saleOrder == null) {
            throw new NotFoundException("Chi tiết đơn hàng không có sẵn");
        }

        Long quantity  = 0L;

        List<OrderItem> orderItems = saleOrder.getOrderItems();

        for (int i = 0; i < orderItems.size(); i++) {
            Product product = orderItems.get(i).getProduct();
            quantity = sizeProductService.getQuantityProductBySizeAndProductId(orderItems.get(i).getSize(), orderItems.get(i).getProduct().getId());
            if (quantity < orderItems.get(i).getQuantity()) {
                throw new AppException("Số lượng của size này không đủ");
            }
        }

        Long totalAmount = saleOrderService.calculateTotalAmount(orderItems);

        if (totalAmount > requestedUser.getAmount()) {
            throw new AppException("Bạn không đủ tiền");
        }

        for (int i = 0; i < orderItems.size(); i++) {
            Product product = orderItems.get(i).getProduct();
            product.setQuantityPurchased(product.getQuantityPurchased() + orderItems.get(i).getQuantity());
//            product.setCurrentNumber(product.getCurrentNumber() - orderItems.get(i).getQuantity());
            SizeProduct sizeProduct = sizeProductService.getSizeProductByProductIdAndSize(product.getId(), orderItems.get(i).getSize());
            sizeProduct.setQuantity(sizeProduct.getQuantity()-orderItems.get(i).getQuantity());
            productService.update(product);
            sizeProductService.update(sizeProduct);
        }

        requestedUser.setAmount(requestedUser.getAmount() - totalAmount);
        userService.update(requestedUser);

        Delivery deliveryWaitingToConfirm = deliveryService.findByWaitingToConfirmState();

        saleOrder.setDelivery(deliveryWaitingToConfirm);
        saleOrder.setOrderedAt(new Timestamp(new Date().getTime()));
        SaleOrder newSaleOrder = saleOrderService.update(saleOrder);

        return this.resSuccess(newSaleOrder);
    }

    @DeleteMapping("/{saleOrderId}")
    @PreAuthorize("@userAuthorizer.isMember(authentication)")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> cancelSaleOrder(@PathVariable("saleOrderId") Long saleOrderId,
                                             HttpServletRequest request) {
        User requestedUser = (User) request.getAttribute("user");

        Delivery deliveryWaitingToConfirm = deliveryService.findByWaitingToConfirmState();

        GenericSpecification<SaleOrder> specification = new GenericSpecification<>();
        specification.add(new SearchCriteria("user", requestedUser.getId(), SearchOperation.EQUAL));
        specification.add(new SearchCriteria("delivery", deliveryWaitingToConfirm.getId(), SearchOperation.EQUAL));
        specification.add(new SearchCriteria("id", saleOrderId, SearchOperation.EQUAL));

        SaleOrder saleOrder = saleOrderService.findOne(specification);

        if (saleOrder == null) {
            throw new NotFoundException("Không tìm thấy chi tiết đơn hàng");
        }

        Delivery deliveryCancel = deliveryService.findByCancelState();

        saleOrder.setDelivery(deliveryCancel);

        List<OrderItem> orderItems = saleOrder.getOrderItems();
        for (OrderItem orderItem : orderItems) {
            Product product = orderItem.getProduct();
            SizeProduct sizeProduct = sizeProductService.getSizeProductByProductIdAndSize(product.getId(), orderItem.getSize());
            product.setCurrentNumber(product.getCurrentNumber() + orderItem.getQuantity());
            sizeProduct.setQuantity(sizeProduct.getQuantity() + orderItem.getQuantity());
            requestedUser.setAmount((orderItem.getQuantity()*orderItem.getProduct().getPrice())+ requestedUser.getAmount());
            productService.update(product);
            userService.update(requestedUser);
            sizeProductService.update(sizeProduct);
        }

        SaleOrder newSaleOrder = saleOrderService.update(saleOrder);

        return this.resSuccess(newSaleOrder);
    }

    @GetMapping("/list")
    @PreAuthorize("@userAuthorizer.isAdmin(authentication)")
    public ResponseEntity<?> getAllDasboard(HttpServletRequest request) {
        User requestedUser = (User) request.getAttribute("user");
        List<SaleOrder> saleOrders = saleOrderService.getALl();
        System.out.println(saleOrders.size());
        DasboardDTO dasboardDTO = new DasboardDTO();
        dasboardDTO.setSaleOrders((long) saleOrders.size());
        Long budget = 0L;
        Set<Long> customerIds = new HashSet<>();
        Long products = 0L;
        for (SaleOrder saleOrder : saleOrders) {
            for (OrderItem orderItem : saleOrder.getOrderItems()) {
                budget += orderItem.getQuantity() * orderItem.getProduct().getPrice();
                products += orderItem.getQuantity();
            }

            customerIds.add(saleOrder.getUser().getId());
        }

        dasboardDTO.setBudget(budget);
        dasboardDTO.setCustomers((long) customerIds.size());
        dasboardDTO.setProducts(products);
        return new ResponseEntity<>(dasboardDTO, HttpStatus.OK);
    }

    @GetMapping("/months")
    @PreAuthorize("@userAuthorizer.isAdmin(authentication)")
    public ResponseEntity<?> getAllDasboardMonth(HttpServletRequest request) {
        User requestedUser = (User) request.getAttribute("user");
        List<DasboardDTO> dasboardDTOS = saleOrderService.getAllMonths();
        return new ResponseEntity<>(dasboardDTOS, HttpStatus.OK);
    }

    @GetMapping("/sale-orders-all")
    @PreAuthorize("@userAuthorizer.isAdmin(authentication)")
    public ResponseEntity<?> getAllSaleOrders(HttpServletRequest request){
        User requestedUser = (User) request.getAttribute("user");
        return this.resListSuccess(saleOrderService.getList());
    }
}
