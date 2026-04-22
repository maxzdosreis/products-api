package com.maxzdosreis.products_api.service;

import com.maxzdosreis.products_api.controller.PurchaseOrderController;
import com.maxzdosreis.products_api.controller.SalesOrderController;
import com.maxzdosreis.products_api.data.dto.*;
import com.maxzdosreis.products_api.exception.BadRequestException;
import com.maxzdosreis.products_api.exception.RequiredObjectIsNullException;
import com.maxzdosreis.products_api.exception.ResourceNotFoundException;
import com.maxzdosreis.products_api.model.*;
import com.maxzdosreis.products_api.model.enums.*;
import com.maxzdosreis.products_api.repository.ProductRepository;
import com.maxzdosreis.products_api.repository.SalesOrderRepository;
import com.maxzdosreis.products_api.repository.spec.PurchaseOrderSpecification;
import com.maxzdosreis.products_api.repository.spec.SalesOrderSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class SalesOrderService {

    private final Logger logger = LoggerFactory.getLogger(SalesOrderService.class);

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockMovementService stockMovementService;

    @Autowired
    private PagedResourcesAssembler<SalesOrderResponseDTO> assembler;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public SalesOrderResponseDTO createSalesOrder(SalesOrderRequestDTO request) {
        if (request == null) throw new RequiredObjectIsNullException();
        logger.info("Creating sales order for customer: {}", request.getCustomerName());

        SalesOrder order = toEntity(request);
        order.recalculateTotalAmount();
        logger.info("Calculated total amount: {}", order.getTotalAmount());

        SalesOrder savedOrder = salesOrderRepository.save(order);
        logger.info("Sales order created with ID: {}", savedOrder.getId());

        SalesOrderResponseDTO response = toDto(savedOrder);
        addHateoasLinks(response);
        return response;
    }

    @Transactional(readOnly = true)
    public PagedModel<EntityModel<SalesOrderResponseDTO>> findAll(Pageable pageable) {
        return findWithFilters(null, null, null, null, null,
                null, null, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public PagedModel<EntityModel<SalesOrderResponseDTO>> findWithFilters(
            String customerName, MatchMode mode, SalesOrderStatus status, SalesOrderDateType dateType,
            LocalDateTime start, LocalDateTime end, BigDecimal min, BigDecimal max,
            List<SalesOrderStatus> statuses, Boolean fullyDelivered, Pageable pageable
    ) {
        logger.info("Finding sales orders with filters - Customer: {}, Status: {}, DateType: {}, Date Range: {} to {}, Total Range: {} to {}, Statuses: {}, FullyDelivered: {}",
                customerName, status, dateType, start, end, min, max, statuses, fullyDelivered);

        Specification<SalesOrder> spec = Specification
                .where(SalesOrderSpecification.customerNameLike(customerName, mode))
                .and(SalesOrderSpecification.hasStatus(status))
                .and(SalesOrderSpecification.totalAmountBetween(min, max))
                .and(SalesOrderSpecification.dateBetween(dateType, start, end))
                .and(SalesOrderSpecification.statusIn(statuses))
                .and(SalesOrderSpecification.isFullyDelivered(fullyDelivered));

        var orders = salesOrderRepository.findAll(spec, pageable).map(o -> {
            SalesOrderResponseDTO response = toDto(o);
            addHateoasLinks(response);
            return response;
        });

        String direction = pageable.getSort().isEmpty() ? "asc"
                : pageable.getSort().stream().findFirst()
                .map(order -> order.isAscending() ? "asc" : "desc")
                .orElse("asc");

        Link selfLink = linkTo(methodOn(SalesOrderController.class)
                .findAll(
                        customerName, mode, status, dateType, start, end,
                        min, max, statuses, fullyDelivered,
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        direction))
                .withSelfRel();
        return assembler.toModel(orders, selfLink);
    }

    @Transactional(readOnly = true)
    public SalesOrderResponseDTO findById(Long id) {
        logger.info("Finding purchase order by ID: {}", id);
        SalesOrderResponseDTO response = toDto(findEntityById(id));
        addHateoasLinks(response);
        return response;
    }

    @Transactional
    public SalesOrderResponseDTO update (Long id, SalesOrderUpdateDTO request) {
        if (request == null) throw new RequiredObjectIsNullException();

        logger.info("Updating sales order with ID: {}", id);

        SalesOrder order = findEntityById(id);

        // Só deve ser possível editar orders que estão em DRAFT
        if (order.getStatus() != SalesOrderStatus.DRAFT) {
            throw new BadRequestException("Apenas ordens em DRAFT podem ser atualizadas. Status atual: " + order.getStatus());
        }

        // Atualiza campos somente se enviar no body
        if (request.getCustomerName() != null && !request.getCustomerName().isBlank()) {
            order.setCustomerName(request.getCustomerName());
        }

        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }

        // Substitui itens somente se a lista veio no body
        if (request.getItems() != null) {
            if (request.getItems().isEmpty()) {
                throw new BadRequestException("A lista de itens não pode ser vazia. Envie null para manter os itens atuais.");
            }

            validateItemsCanBeReplaced(order);
            
            order.getItems().clear();
            for (SalesOrderItemRequestDTO itemDto : request.getItems()) {
                Product product = productService.findEntityById(itemDto.getProductId());
                SalesOrderItem item = SalesOrderItem.builder()
                        .salesOrder(order)
                        .product(product)
                        .quantity(itemDto.getQuantity())
                        .unitPrice(itemDto.getUnitPrice())
                        .build();
                order.getItems().add(item);
            }
            order.recalculateTotalAmount();
        }

        SalesOrderResponseDTO response = toDto(salesOrderRepository.save(order));
        addHateoasLinks(response);
        return response;
    }



    // Ao confirmar, dispara SAIDA no estoque para cada item
    @Transactional
    public SalesOrderResponseDTO confirm(Long id) {
        logger.info("Confirming sales order id={}", id);
        SalesOrder order = findEntityById(id);

        if (order.getStatus() != SalesOrderStatus.DRAFT) {
            throw new BadRequestException("Apenas ordens em DRAFT podem ser confirmadas. Status atual: " + order.getStatus());
        }

        if (order.getItems().isEmpty()) {
            throw new BadRequestException("O pedido não pode ser confirmado sem itens.");
        }

        for (SalesOrderItem item : order.getItems()) {
            StockMovementRequestDto movement = StockMovementRequestDto.builder()
                    .type(StockMovement.MovementType.SAIDA)
                    .quantity(item.getQuantity())
                    .reason("Venda SO-" + id)
                    .build();
            stockMovementService.registerMovement(item.getProduct().getId(), movement);
        }

        order.setStatus(SalesOrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        SalesOrderResponseDTO response = toDto(salesOrderRepository.save(order));
        addHateoasLinks(response);
        return response;
    }

    @Transactional
    public SalesOrderResponseDTO ship(Long id) {
        logger.info("Shipping sales order id={}", id);
        SalesOrder order = findEntityById(id);

        if (order.getStatus() != SalesOrderStatus.CONFIRMED) {
            throw new BadRequestException("Apenas ordens CONFIRMED podem ser enviadas. Status atual: " + order.getStatus());
        }

        order.setStatus(SalesOrderStatus.SHIPPED);
        order.setShippedAt(LocalDateTime.now());
        SalesOrderResponseDTO response = toDto(salesOrderRepository.save(order));
        addHateoasLinks(response);
        return response;
    }

    @Transactional
    public SalesOrderResponseDTO deliver(Long id) {
        logger.info("Shipping sales order id={}", id);
        SalesOrder order = findEntityById(id);

        if (order.getStatus() != SalesOrderStatus.SHIPPED) {
            throw new BadRequestException("Apenas pedidos SHIPPED podem ser entregues. Status atual: " + order.getStatus());
        }

        order.setStatus(SalesOrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        SalesOrderResponseDTO response = toDto(salesOrderRepository.save(order));
        addHateoasLinks(response);
        return response;
    }

    @Transactional
    public SalesOrderResponseDTO cancel(Long id) {
        logger.info("Cancelling sales order id={}", id);
        SalesOrder order = findEntityById(id);

        if (order.getStatus() == SalesOrderStatus.DELIVERED) {
            throw new BadRequestException("Pedidos já entregues não podem ser cancelados.");
        }

        if (order.getStatus() == SalesOrderStatus.CANCELLED) {
            throw new BadRequestException("Pedido já está cancelado.");
        }

        // Se já estava CONFIRMED, estorno de estoque (ENTRADA de volta)
        if (order.getStatus() == SalesOrderStatus.CONFIRMED || order.getStatus() == SalesOrderStatus.SHIPPED) {
            for (SalesOrderItem item : order.getItems()) {
                StockMovementRequestDto movement = StockMovementRequestDto.builder()
                        .type(StockMovement.MovementType.ENTRADA)
                        .quantity(item.getQuantity())
                        .reason("Cancelamento SO-" + id)
                        .build();
                stockMovementService.registerMovement(item.getProduct().getId(), movement);
            }
        }

        order.setStatus(SalesOrderStatus.CANCELLED);
        SalesOrderResponseDTO response = toDto(salesOrderRepository.save(order));
        addHateoasLinks(response);
        return response;
    }

    protected SalesOrder findEntityById(Long id) {
        logger.info("Finding sales order by ID: {}", id);
        return salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with ID: " + id));
    }

    private void validateItemsCanBeReplaced(SalesOrder order) {
        logger.warn("Validating if items can be replaced for order ID: {}", order.getId());

        if (!order.getItems().isEmpty()) {
            boolean hasAnyItemWithData = order.getItems().stream()
                    .anyMatch(item -> item.getQuantity() != null && item.getQuantity().signum() > 0);

            if (hasAnyItemWithData && order.getStatus() != SalesOrderStatus.DRAFT) {
                throw new BadRequestException(
                        "Não é possível substituir itens de uma ordem que já foi processada. " +
                                "Status atual: " + order.getStatus() + ". " +
                                "Para remover itens, cancele a ordem e crie uma nova."
                );
            }
        }

        logger.info("Items validation passed for order ID: {}", order.getId());
    }

    private SalesOrder toEntity(SalesOrderRequestDTO dto) {
        List<SalesOrderItem> items = dto.getItems().stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + item.getProductId()));
                    return SalesOrderItem.builder()
                            .product(product)
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .build();
                })
                .toList();
        SalesOrder order = SalesOrder.builder()
                .customerName(dto.getCustomerName())
                .notes(dto.getNotes())
                .items(items)
                .build();

        items.forEach(item -> item.setSalesOrder(order));

        return order;
    }

    private SalesOrderResponseDTO toDto(SalesOrder entity) {
        List<SalesOrderItemResponseDTO> items = entity.getItems().stream()
                .map(item -> SalesOrderItemResponseDTO.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return SalesOrderResponseDTO.builder()
                .id(entity.getId())
                .customerName(entity.getCustomerName())
                .status(entity.getStatus())
                .totalAmount(entity.getTotalAmount())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .confirmedAt(entity.getConfirmedAt())
                .shippedAt(entity.getShippedAt())
                .deliveredAt(entity.getDeliveredAt())
                .items(items)
                .build();
    };

    private void addHateoasLinks(SalesOrderResponseDTO dto) {
        dto.add(linkTo(methodOn(SalesOrderController.class).findAll(null, null, null, null, null, null, null, null, null, null, 0, 12, "asc")).withRel("findAll").withType("GET"));
        dto.add(linkTo(methodOn(SalesOrderController.class).create(null)).withRel("create").withType("POST"));
        dto.add(linkTo(methodOn(SalesOrderController.class).findById(dto.getId())).withSelfRel().withType("GET"));
        dto.add(linkTo(methodOn(SalesOrderController.class).update(dto.getId(), null)).withRel("update").withType("PUT"));
        dto.add(linkTo(methodOn(SalesOrderController.class).confirm(dto.getId())).withRel("confirm").withType("PATCH"));
        dto.add(linkTo(methodOn(SalesOrderController.class).ship(dto.getId())).withRel("ship").withType("PATCH"));
        dto.add(linkTo(methodOn(SalesOrderController.class).deliver(dto.getId())).withRel("deliver").withType("PATCH"));
        dto.add(linkTo(methodOn(SalesOrderController.class).cancel(dto.getId())).withRel("cancel").withType("PATCH"));
    }
}
