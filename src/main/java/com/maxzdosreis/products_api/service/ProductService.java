package com.maxzdosreis.products_api.service;

import com.maxzdosreis.products_api.data.dto.ProductDto;
import com.maxzdosreis.products_api.exception.RequiredObjectIsNullException;
import com.maxzdosreis.products_api.exception.ResourceNotFoundException;
import com.maxzdosreis.products_api.model.Product;
import com.maxzdosreis.products_api.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.maxzdosreis.products_api.mapper.ObjectMapper.parseObject;
import static com.maxzdosreis.products_api.mapper.ObjectMapper.parseListObjects;

@Service
public class ProductService {

    private Logger logger = LoggerFactory.getLogger(ProductService.class.getName());

    @Autowired
    ProductRepository productRepository;

    public ProductDto createProduct(ProductDto productDto) {

        if (productDto == null) throw new RequiredObjectIsNullException();

        logger.info("Creating one Product");
        var entity = parseObject(productDto, Product.class);

        var dto =  parseObject(productRepository.save(entity), ProductDto.class);
        return dto;
    }

    public List<ProductDto> findAll(){
        logger.info("Finding all Products");
        var entity = parseListObjects(productRepository.findAll(), ProductDto.class);
        return entity;
    }

    public ProductDto updateProduct(Long id, ProductDto productDto) {
        if (productDto == null) throw new RequiredObjectIsNullException();

        logger.info("Updating one Product");
        Product entity = productRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Product not found"));
        entity.setName(productDto.getName());
        entity.setDescription(productDto.getDescription());
        entity.setPrice(productDto.getPrice());
        entity.setQuantity(productDto.getQuantity());

        var dto = parseObject(productRepository.save(entity), ProductDto.class);
        return dto;
    }

    public ProductDto findById(Long id) {
        logger.info("Finding one Product");

        var entity = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        var dto = parseObject(entity, ProductDto.class);
        return dto;
    }

    public void delete(Long id) {
        logger.info("Deleting one Product");
        Product entity = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        productRepository.delete(entity);
    }
}
