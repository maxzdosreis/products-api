package com.maxzdosreis.products_api.data.dto;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Double price;
    private String description;
    private Integer quantity;
}
