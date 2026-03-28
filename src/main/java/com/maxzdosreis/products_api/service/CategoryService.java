package com.maxzdosreis.products_api.service;

import com.maxzdosreis.products_api.controller.CategoryController;
import com.maxzdosreis.products_api.controller.ProductController;
import com.maxzdosreis.products_api.data.dto.CategoryDTO;
import com.maxzdosreis.products_api.exception.BadRequestException;
import com.maxzdosreis.products_api.exception.RequiredObjectIsNullException;
import com.maxzdosreis.products_api.exception.ResourceNotFoundException;
import com.maxzdosreis.products_api.model.Category;
import com.maxzdosreis.products_api.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class CategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PagedResourcesAssembler<CategoryDTO> assembler;

    public CategoryDTO createCategory(CategoryDTO categoryDTO) {

        if (categoryDTO == null) throw new RequiredObjectIsNullException();
        logger.info("Creating category: {}", categoryDTO.getId());

        validateCategoryDto(categoryDTO, null);

        Category entity = toEntity(categoryDTO);

        CategoryDTO result = toDto(categoryRepository.save(entity));
        addHateoasLink(result);
        return result;
    }

    //@Transactional(readOnly = true)
    public PagedModel<EntityModel<CategoryDTO>> findAll(Pageable pageable) {
        logger.info("Finding all categories");
        var categories = categoryRepository.findAll(pageable).map(c -> {
            CategoryDTO categoryDTO = toDto(c);
            addHateoasLink(categoryDTO);
            return categoryDTO;
        });

        Link selfLink = linkTo(methodOn(CategoryController.class)
                .findAll(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        String.valueOf(pageable.getSort())))
                .withSelfRel();
        return assembler.toModel(categories, selfLink);
    }

    public PagedModel<EntityModel<CategoryDTO>> findByName(String name, Pageable pageable) {
        logger.info("Finding category by name: {}", name);

        var categories = categoryRepository.findByNameContaining(name, pageable).map(c -> {
            CategoryDTO categoryDTO = toDto(c);
            addHateoasLink(categoryDTO);
            return categoryDTO;
        });

        Link selfLink = linkTo(methodOn(CategoryController.class)
                .findByName(
                        name,
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        String.valueOf(pageable.getSort())))
                .withSelfRel();
        return assembler.toModel(categories, selfLink);
    }

    public CategoryDTO findById(Long id) {
        logger.info("Finding category id={}", id);

        Category entity = findEntityById(id);
        CategoryDTO dto = toDto(entity);
        addHateoasLink(dto);
        return dto;
    }

    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        if (categoryDTO == null) throw new RequiredObjectIsNullException();

        logger.info("Updating category id={}", id);

        Category entity = findEntityById(id);
        validateCategoryDto(categoryDTO, entity);

        entity.setName(categoryDTO.getName());
        entity.setDescription(categoryDTO.getDescription());

        CategoryDTO result = toDto(categoryRepository.save(entity));
        addHateoasLink(result);
        return result;
    }

    @Transactional
    public CategoryDTO enableCategory(Long id) {
        logger.info("Enabling category id={}", id);

        findEntityById(id);
        categoryRepository.enableCategory(id);

        CategoryDTO dto = toDto(categoryRepository.findById(id).get());
        addHateoasLink(dto);
        return dto;
    }

    @Transactional
    public CategoryDTO disableCategory(Long id) {
        logger.info("Disabling category id={}", id);

        findEntityById(id);
        categoryRepository.disableCategory(id);

        CategoryDTO dto = toDto(categoryRepository.findById(id).get());
        addHateoasLink(dto);
        return dto;
    }

    public void delete (Long id) {
        logger.info("Deleting category id={}", id);
        Category entity = findEntityById(id);
        categoryRepository.delete(entity);
    }

    private Category findEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com ID: " + id));
    }

    private void validateCategoryDto(CategoryDTO categoryDto, Category existing) {
        // Verifica nome duplicado
        boolean nameChanged = existing == null || !existing.getName().equals(categoryDto.getName());
        if (nameChanged && categoryRepository.existsByName(categoryDto.getName())) {
            throw new BadRequestException("Já existe uma categoria com o nome: " + categoryDto.getName());
        }
    }

    private Category toEntity(CategoryDTO categoryDto) {
        return Category.builder()
                .id(categoryDto.getId())
                .name(categoryDto.getName())
                .description(categoryDto.getDescription())
                .build();
    }

    private CategoryDTO toDto(Category entity) {
        return CategoryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .enabled(entity.getEnabled())
                .build();
    }

    private void addHateoasLink(CategoryDTO categoryDTO) {
        categoryDTO.add(linkTo(methodOn(CategoryController.class).findById(categoryDTO.getId())).withSelfRel().withType("GET"));
        categoryDTO.add(linkTo(methodOn(CategoryController.class).findAll(1,12,"asc")).withRel("findAll").withType("GET"));
        categoryDTO.add(linkTo(methodOn(CategoryController.class).findByName(categoryDTO.getName(),1,12,"asc")).withRel("findByName").withType("GET"));
        categoryDTO.add(linkTo(methodOn(CategoryController.class).create(categoryDTO)).withRel("create").withType("POST"));
        categoryDTO.add(linkTo(methodOn(CategoryController.class).update(categoryDTO.getId(), categoryDTO)).withRel("update").withType("PUT"));
        categoryDTO.add(linkTo(methodOn(CategoryController.class).enableCategory(categoryDTO.getId())).withRel("enable").withType("PATCH"));
        categoryDTO.add(linkTo(methodOn(CategoryController.class).disableProduct(categoryDTO.getId())).withRel("disable").withType("PATCH"));
        categoryDTO.add(linkTo(methodOn(CategoryController.class).delete(categoryDTO.getId())).withRel("delete").withType("DELETE"));
    }
}
