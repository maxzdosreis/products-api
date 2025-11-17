package com.maxzdosreis.products_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 250)
    private String name;

    @NotNull
    @Positive
    private Double price;

    @NotBlank
    private String description;

    @NotNull
    @Positive
    private Integer quantity;

    @Column(nullable = false, columnDefinition = "BIT(1) DEFAULT b'1'")
    private Boolean enabled = true;
}
