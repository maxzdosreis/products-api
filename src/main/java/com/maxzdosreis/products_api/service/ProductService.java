package com.maxzdosreis.products_api.service;

import com.maxzdosreis.products_api.controller.ProductController;
import com.maxzdosreis.products_api.data.dto.ProductDto;
import com.maxzdosreis.products_api.exception.BadRequestException;
import com.maxzdosreis.products_api.exception.RequiredObjectIsNullException;
import com.maxzdosreis.products_api.exception.ResourceNotFoundException;
import com.maxzdosreis.products_api.model.Product;
import com.maxzdosreis.products_api.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static com.maxzdosreis.products_api.mapper.ObjectMapper.parseObject;
import static com.maxzdosreis.products_api.mapper.ObjectMapper.parseListObjects;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class ProductService {

    private Logger logger = LoggerFactory.getLogger(ProductService.class.getName());

    @Autowired
    PagedResourcesAssembler<ProductDto> assembler;

    @Autowired
    ProductRepository productRepository;

    public ProductDto createProduct(ProductDto productDto) {

        if (productDto == null) throw new RequiredObjectIsNullException();

        logger.info("Creating one Product: {}", productDto.getName());

        validateProductDto(productDto, null);

        Product entity = toEntity(productDto);
        // Começa em zero na criação
        entity.setCurrentStock(BigDecimal.ZERO);

        ProductDto result = toDto(productRepository.save(entity));
        addHateoasLinks(result);
        return result;
    }

    public PagedModel<EntityModel<ProductDto>> findAll(Pageable pageable){
        logger.info("Finding all Products");
        var products = productRepository.findAll(pageable).map(p -> {
            ProductDto productDto = toDto(p);
            addHateoasLinks(productDto);
            return productDto;
        });

        Link selfLink = linkTo(methodOn(ProductController.class)
                .findAll(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        String.valueOf(pageable.getSort())))
                .withSelfRel();
        return assembler.toModel(products, selfLink);
    }

    public PagedModel<EntityModel<ProductDto>> findByName(String name, Pageable pageable) {
        logger.info("Finding Product by name: {}", name);

        var products = productRepository.findProductByName(name, pageable).map(p -> {
            ProductDto productDto = toDto(p);
            addHateoasLinks(productDto);
            return productDto;
        });

        Link selfLink = linkTo(methodOn(ProductController.class)
                .findByName(
                        name,
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        String.valueOf(pageable.getSort())))
                .withSelfRel();
        return assembler.toModel(products, selfLink);
    }

    public ProductDto findById(Long id) {
        logger.info("Finding product id={}", id);

        Product entity = findEntityById(id);
        ProductDto dto = toDto(entity);
        addHateoasLinks(dto);
        return dto;
    }

    public ProductDto updateProduct(Long id, ProductDto productDto) {
        if (productDto == null) throw new RequiredObjectIsNullException();

        logger.info("Updating product id={}", id);

        Product entity = findEntityById(id);
        validateProductDto(productDto, entity);

        entity.setName(productDto.getName());
        entity.setDescription(productDto.getDescription());
        entity.setUnit(productDto.getUnit());
        entity.setType(productDto.getType());
        entity.setCostPrice(productDto.getCostPrice());
        entity.setSalePrice(productDto.getSalePrice());
        entity.setMinStock(productDto.getMinStock());
        entity.setMaxStock(productDto.getMaxStock());
        entity.setRequiresBatchControl(
                productDto.getRequiresBatchControl() != null ? productDto.getRequiresBatchControl() : false);
        entity.setRequiresExpiryControl(
                productDto.getRequiresExpiryControl() != null ? productDto.getRequiresExpiryControl() : false);

        ProductDto result = toDto(productRepository.save(entity));
        addHateoasLinks(result);
        return result;
    }

    @Transactional
    public ProductDto enableProduct(Long id) {
        logger.info("Enabling product id={}", id);

        findEntityById(id);
        productRepository.enableProduct(id);

        ProductDto dto = toDto(productRepository.findById(id).get());
        addHateoasLinks(dto);
        return dto;
    }

    @Transactional
    public ProductDto disableProduct(Long id) {
        logger.info("Disabling product id={}", id);

        findEntityById(id);
        productRepository.disableProduct(id);

        ProductDto dto = toDto(productRepository.findById(id).get());
        addHateoasLinks(dto);
        return dto;
    }

    public void delete(Long id) {
        logger.info("Deleting product id={}", id);
        Product entity = findEntityById(id);
        productRepository.delete(entity);
    }

    private Product findEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com ID: " + id));
    }

    private void validateProductDto(ProductDto productDto, Product existing) {
        // Verifica nome duplicado
        boolean nameChanged = existing == null || !existing.getName().equals(productDto.getName());
        if (nameChanged && productRepository.existsByName(productDto.getName())) {
            throw new BadRequestException("Já existe um produto com o nome: " + productDto.getName());
        }

        // Valida intervalo de estoque
        if (productDto.getMinStock() != null && productDto.getMaxStock() != null) {
            if (productDto.getMinStock().compareTo(productDto.getMaxStock()) > 0) {
                throw new BadRequestException("Estoque mínimo não pode ser maior que o estoque máximo");
            }
        }

        // Valida margens de preço
        if (productDto.getCostPrice() != null && productDto.getSalePrice() != null) {
            if (productDto.getCostPrice().compareTo(productDto.getSalePrice()) > 0) {
                throw new BadRequestException("Preço de venda não pode ser menor que o preço de custo");
            }
        }
    }

    private Product toEntity(ProductDto productDto) {
        return Product.builder()
                .id(productDto.getId())
                .name(productDto.getName())
                .description(productDto.getDescription())
                .unit(productDto.getUnit())
                .type(productDto.getType())
                .costPrice(productDto.getCostPrice())
                .salePrice(productDto.getSalePrice())
                .minStock(productDto.getMinStock())
                .maxStock(productDto.getMaxStock())
                .currentStock(productDto.getCurrentStock() !=  null ? productDto.getCurrentStock() : BigDecimal.ZERO)
                .requiresBatchControl(productDto.getRequiresBatchControl() != null ? productDto.getRequiresBatchControl() : false)
                .requiresExpiryControl(productDto.getRequiresExpiryControl() != null ? productDto.getRequiresExpiryControl() : false)
                .build();
    }

    private ProductDto toDto(Product entity) {
        return ProductDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .unit(entity.getUnit())
                .type(entity.getType())
                .costPrice(entity.getCostPrice())
                .salePrice(entity.getSalePrice())
                .minStock(entity.getMinStock())
                .maxStock(entity.getMaxStock())
                .currentStock(entity.getCurrentStock())
                .requiresBatchControl(entity.getRequiresBatchControl())
                .requiresExpiryControl(entity.getRequiresExpiryControl())
                .enabled(entity.getEnabled())
                .build();
    }

    private void addHateoasLinks(ProductDto productDto) {
        productDto.add(linkTo(methodOn(ProductController.class).findById(productDto.getId())).withSelfRel().withType("GET"));
        productDto.add(linkTo(methodOn(ProductController.class).findAll(1,12,"asc")).withRel("findAll").withType("GET"));
        productDto.add(linkTo(methodOn(ProductController.class).findByName(productDto.getName(),1,12,"asc")).withRel("findByName").withType("GET"));
        productDto.add(linkTo(methodOn(ProductController.class).create(productDto)).withRel("create").withType("POST"));
        productDto.add(linkTo(methodOn(ProductController.class).update(productDto.getId(), productDto)).withRel("update").withType("PUT"));
        productDto.add(linkTo(methodOn(ProductController.class).enableProduct(productDto.getId())).withRel("enable").withType("PATCH"));
        productDto.add(linkTo(methodOn(ProductController.class).disableProduct(productDto.getId())).withRel("disable").withType("PATCH"));
        productDto.add(linkTo(methodOn(ProductController.class).delete(productDto.getId())).withRel("delete").withType("DELETE"));
    }
}
