package com.bookshop.controllers;

import com.bookshop.base.BaseController;
import com.bookshop.dao.SaleOrder;
import com.bookshop.dao.SizeProduct;
import com.bookshop.dto.SizeProductDTO;
import com.bookshop.dto.pagination.PaginateDTO;
import com.bookshop.services.SizeProductService;
import com.bookshop.specifications.GenericSpecification;
import com.bookshop.specifications.JoinCriteria;
import com.bookshop.specifications.SearchOperation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.JoinType;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/size-products")
@SecurityRequirement(name = "Authorization")
public class SizeProductController extends BaseController<SizeProduct> {
    @Autowired
    private SizeProductService sizeProductService;


    @GetMapping("/{productId}")
    public ResponseEntity<?> getAllSizeProductByProductId(@PathVariable(name = "productId") Long productId){
        return new ResponseEntity<>(sizeProductService.getAllSizeByProductId(productId), HttpStatus.OK);
    }

    @GetMapping("/quantity/{productId}")
    public ResponseEntity<?> getQuantityBySizeAndProductId(@PathVariable(name = "productId") Long productId, @RequestParam(name = "size") String size){
        return new ResponseEntity<>(sizeProductService.getQuantityProductBySizeAndProductId(size, productId), HttpStatus.OK);
    }

    @GetMapping
//    @PreAuthorize("@userAuthorizer.isAdmin(authentication)")
    public ResponseEntity<?> getAll(@RequestParam(name = "page", required = false) Integer page,
                                    @RequestParam(name = "perPage", required = false) Integer perPage,
                                    @RequestParam(name = "category", required = false) String category,
                                    HttpServletRequest request){
        GenericSpecification<SizeProduct> specification = new GenericSpecification<SizeProduct>().getBasicQuery(request);

        if(category != null)
            specification.buildJoin(new JoinCriteria(SearchOperation.EQUAL, "product", "id", category, JoinType.INNER));
        PaginateDTO<SizeProduct> sizeProductPaginateDTO = sizeProductService.getList(page, perPage, specification);

        return this.resPagination(sizeProductPaginateDTO);
    }

    @PostMapping("/change")
    public ResponseEntity<?> addOrUpdate(@RequestBody SizeProductDTO sizeProductDTO){
        sizeProductService.addOrUpdateSizeProduct(sizeProductDTO);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
}
