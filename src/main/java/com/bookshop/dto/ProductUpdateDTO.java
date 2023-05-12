package com.bookshop.dto;

import com.bookshop.constants.Common;
import com.bookshop.validators.NullOrNotEmpty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductUpdateDTO {

    @NullOrNotEmpty(message = "is invalid")
    @Length(max = Common.STRING_LENGTH_LIMIT)
    private String title;

    @NullOrNotEmpty(message = "is invalid")
    @Length(max = 100000)
    private String longDescription;

    @NullOrNotEmpty(message = "is invalid")
    @Min(1)
    private Long categoryId;

    @NullOrNotEmpty(message = "is invalid")
    @Min(1)
    private Long price;

    @NullOrNotEmpty(message = "is invalid")
    @Length(max = Common.STRING_LENGTH_LIMIT)
    private String author;

    @NullOrNotEmpty(message = "is invalid")
    @Min(0)
    private Integer currentNumber;

    @NullOrNotEmpty(message = "is invalid")
    @Min(1)
    private Integer numberOfPage;

    private SizeProductDTO sizeProductDTO = new SizeProductDTO();
}
