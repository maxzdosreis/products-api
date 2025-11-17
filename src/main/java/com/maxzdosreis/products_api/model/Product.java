package com.maxzdosreis.products_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 250, message = "Nome deve ter entre 3 e 250 caracteres")
    @Column(nullable = false, length = 250, unique = true)
    private String name;

    @NotNull(message = "Preço é obrigatório")
    @Positive(message = "Preço deve ser maior que zero")
    @Column(nullable = false)
    private Double price;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 3, max = 250, message = "Descrição deve ter entre 3 e 250 caracteres")
    @Column(nullable = false, length = 250)
    private String description;

    @NotNull(message = "Quantidade é obrigatória")
    @PositiveOrZero(message = "Quantidade deve ser maior ou igual a zero")
    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Boolean enabled = true;
}
