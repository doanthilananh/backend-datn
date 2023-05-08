package com.bookshop.controllers;

import com.bookshop.services.SizeProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/size-products")
public class SizeProductController {
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
}
