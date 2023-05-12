package com.bookshop.base;

import com.bookshop.dto.pagination.PaginateDTO;
import com.bookshop.dto.pagination.PaginationDTO;
import com.bookshop.dto.pagination.PaginationResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BaseControllerDTO<E> {
    public ResponseEntity<?> resPaginationDTO(PaginateDTO<?> paginateDTO, List<E> dtos, Integer page, Integer perPage) {

        if (page == null || page <= 0)
            page = 1;
        if (perPage == null || perPage <= 0)
            perPage = 10;

        Page<E> pageData = new PageImpl<>(dtos, PageRequest.of(page, perPage, Sort.by("quantity").descending()),
                paginateDTO.getPageData().getTotalElements());

        PaginationDTO<List<E>> paginationDTO = new PaginationDTO<>(pageData.getContent(), paginateDTO.getPagination());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new PaginationResponseDTO<>(HttpStatus.OK.value(), "Success", paginationDTO));
    }
}