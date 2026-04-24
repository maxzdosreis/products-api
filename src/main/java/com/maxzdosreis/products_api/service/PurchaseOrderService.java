package com.maxzdosreis.products_api.service;

import com.maxzdosreis.products_api.controller.ProductController;
import com.maxzdosreis.products_api.controller.PurchaseOrderController;
import com.maxzdosreis.products_api.data.dto.*;
import com.maxzdosreis.products_api.exception.BadRequestException;
import com.maxzdosreis.products_api.exception.RequiredObjectIsNullException;
import com.maxzdosreis.products_api.exception.ResourceNotFoundException;
import com.maxzdosreis.products_api.model.Product;
import com.maxzdosreis.products_api.model.PurchaseOrder;
import com.maxzdosreis.products_api.model.PurchaseOrderItem;
import com.maxzdosreis.products_api.model.StockMovement.MovementType;
import com.maxzdosreis.products_api.model.enums.MatchMode;
import com.maxzdosreis.products_api.model.enums.PurchaseOrderDateType;
import com.maxzdosreis.products_api.repository.PurchaseOrderRepository;
import com.maxzdosreis.products_api.model.enums.PurchaseOrderStatus;
import com.maxzdosreis.products_api.repository.ProductRepository;
import com.maxzdosreis.products_api.repository.spec.PurchaseOrderSpecification;
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
public class PurchaseOrderService {

    private Logger logger = LoggerFactory.getLogger(PurchaseOrderService.class);

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockMovementService stockMovementService;

    @Autowired
    private PagedResourcesAssembler<PurchaseOrderResponseDTO> assembler;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public PurchaseOrderResponseDTO createPurchaseOrder(PurchaseOrderRequestDTO request) {
        if (request == null) throw new RequiredObjectIsNullException();
        logger.info("Creating purchase order for supplier: {}", request.getSupplierName());

        PurchaseOrder order = toEntity(request);
        order.recalculateTotalAmount();
        logger.info("Calculated total amount: {}", order.getTotalAmount());

        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);
        logger.info("Purchase order created with ID: {}", savedOrder.getId_po());

        PurchaseOrderResponseDTO response = toDto(savedOrder);
        addHateoasLinks(response);
        return response;
    }

    @Transactional(readOnly = true)
    public PagedModel<EntityModel<PurchaseOrderResponseDTO>> findAll(Pageable pageable) {
        return findWithFilters(null, null, null, null, null,
                null, null, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public PagedModel<EntityModel<PurchaseOrderResponseDTO>> findWithFilters(
            String supplierName, MatchMode mode, PurchaseOrderStatus status, PurchaseOrderDateType dateType,
            LocalDateTime start, LocalDateTime end, BigDecimal min, BigDecimal max,
            List<PurchaseOrderStatus> statuses, Boolean fullyReceived, Pageable pageable
    ) {
        logger.info("Finding purchase orders with filters - Supplier: {}, Status: {}, DateType: {}, Date Range: {} to {}, Total Range: {} to {}, Statuses: {}, FullyReceived: {}",
                supplierName, status, dateType, start, end, min, max, statuses, fullyReceived);

        Specification<PurchaseOrder> spec = Specification
                .where(PurchaseOrderSpecification.supplierNameLike(supplierName, mode))
                .and(PurchaseOrderSpecification.hasStatus(status))
                .and(PurchaseOrderSpecification.totalAmountBetween(min, max))
                .and(PurchaseOrderSpecification.dateBetween(dateType, start, end))
                .and(PurchaseOrderSpecification.statusIn(statuses))
                .and(PurchaseOrderSpecification.isFullyReceived(fullyReceived));

        var orders = purchaseOrderRepository.findAll(spec, pageable).map(o -> {
            PurchaseOrderResponseDTO response = toDto(o);
            addHateoasLinks(response);
            return response;
        });

         String direction = pageable.getSort().isEmpty() ? "asc"
                 : pageable.getSort().stream().findFirst()
                 .map(order -> order.isAscending() ? "asc" : "desc")
                 .orElse("asc");

         Link selfLink = linkTo(methodOn(PurchaseOrderController.class)
                 .findAll(
                         supplierName, mode, status, dateType, start, end,
                         min, max, statuses, fullyReceived,
                         pageable.getPageNumber(),
                         pageable.getPageSize(),
                         direction))
                 .withSelfRel();
        return assembler.toModel(orders, selfLink);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponseDTO findById(Long id) {
        logger.info("Finding purchase order by ID: {}", id);
        PurchaseOrderResponseDTO response = toDto(findEntityById(id));
        addHateoasLinks(response);
        return response;
    }

    @Transactional
    public PurchaseOrderResponseDTO update(Long id, PurchaseOrderUpdateDTO request) {
        if (request == null) throw new RequiredObjectIsNullException();

        logger.info("Updating purchase order ID: {} with supplier: {}", id, request.getSupplierName());

        PurchaseOrder order = findEntityById(id);

        // Só deve ser possível editar orders que estão em DRAFT
        if (order.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new BadRequestException("Apenas ordens em DRAFT podem ser atualizadas. Status atual: " + order.getStatus());
        }

        // Atualiza campos somente se vierem no body
        if (request.getSupplierName() != null && !request.getSupplierName().isBlank()) {
            order.setSupplierName(request.getSupplierName());
        }
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }

        // Substitui itens somente se a lista veio no body
        if (request.getItems() != null) {
            if (request.getItems().isEmpty()) {
                throw new BadRequestException("A lista de itens não pode ser vazia. Envie null para manter os itens atuais");
            }
            
            validateItemsCanBeReplaced(order);
            
            order.getItems().clear();
            for (PurchaseOrderItemRequestDTO itemDto : request.getItems()) {
                Product product = productService.findEntityById(itemDto.getProductId());
                PurchaseOrderItem item = PurchaseOrderItem.builder()
                        .purchaseOrder(order)
                        .product(product)
                        .quantity(itemDto.getQuantity())
                        .unitPrice(itemDto.getUnitPrice())
                        .build();
                order.getItems().add(item);
            }
            order.recalculateTotalAmount();
        }

        PurchaseOrderResponseDTO response = toDto(purchaseOrderRepository.save(order));
        addHateoasLinks(response);
        return response;
    }

    @Transactional
    public PurchaseOrderResponseDTO confirm(Long id) {
        logger.info("Confirming purchase order by ID: {}", id);
        PurchaseOrder order = findEntityById(id);

        if (order.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new BadRequestException("Apenas ordens em DRAFT podem ser confirmadas. Status atual: " + order.getStatus());
        }

        if (order.getItems().isEmpty()) {
            throw new BadRequestException("A ordem não pode ser confirmada sem itens.");
        }

        order.setStatus(PurchaseOrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        PurchaseOrderResponseDTO response = toDto(purchaseOrderRepository.save(order));
        addHateoasLinks(response);
        return response;
    }

    // Registra entrada no estoque para cada item recebido
    @Transactional
    public PurchaseOrderResponseDTO receiveItems(Long id, List<PurchaseOrderItemRequestDTO> receivedItems) {
        logger.info("Receiving purchase order items by ID: {}", id);

        if (receivedItems == null || receivedItems.isEmpty()) {
            throw new BadRequestException("A lista de itens recebidos não pode estar vazia.");
        }
        
        PurchaseOrder order = findEntityById(id);

        if (order.getStatus() != PurchaseOrderStatus.CONFIRMED
                && order.getStatus() != PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            throw new BadRequestException("Apenas ordens CONFIRMED ou PARTIALLY_RECEIVED podem ter itens recebidos. Status atual: " + order.getStatus());
        }

        for (PurchaseOrderItemRequestDTO received : receivedItems) {

            if (received.getQuantity() == null || received.getQuantity().signum() <= 0) {
                throw new BadRequestException("Quantidade recebida deve ser maior que zero para produto ID: " + received.getProductId());
            }
            
            PurchaseOrderItem item = order.getItems().stream()
                    .filter(i -> i.getProduct().getId().equals(received.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Produto ID: " + received.getProductId() + " não encontrado na ordem."));

            if (item.getReceivedQuantity().add(received.getQuantity()).compareTo(item.getQuantity()) > 0) {
                throw new BadRequestException("Quantidade recebida (" + received.getQuantity() + ") excede a quantidade pedida (" + item.getQuantity() + ") para produto: " + item.getProduct().getName());
            }

            item.setReceivedQuantity(item.getReceivedQuantity().add(received.getQuantity()));
            logger.info("Received {} units of product ID: {} (total received: {})", received.getQuantity(), item.getProduct().getId(), item.getReceivedQuantity());

            // Dispara ENTRADA no estoque via StockMovementService
            StockMovementRequestDto movement = StockMovementRequestDto.builder()
                    .type(MovementType.ENTRADA)
                    .quantity(received.getQuantity())
                    .reason("Recebimento PO-" + id)
                    .build();
            stockMovementService.registerMovement(item.getProduct().getId(), movement);
            logger.info("Stock movement registered for product ID: {}", item.getProduct().getId());
        }

        // Determina novo status
        boolean allReceived = order.getItems().stream()
                .allMatch(PurchaseOrderItem::isFullyReceived);
        PurchaseOrderStatus previousStatus = order.getStatus();
        order.setStatus(allReceived ? PurchaseOrderStatus.RECEIVED : PurchaseOrderStatus.PARTIALLY_RECEIVED);
        
        if (allReceived) {
            order.setReceivedAt(LocalDateTime.now());
            logger.info("All items received. Purchase order status changed to RECEIVED.");
        } else {
            logger.info("Purchase order status changed from {} to PARTIALLY_RECEIVED.", previousStatus);
        }
        
        PurchaseOrderResponseDTO response = toDto(purchaseOrderRepository.save(order));
        addHateoasLinks(response);
        return response;
    }

    @Transactional
    public PurchaseOrderResponseDTO cancel(Long id) {
        logger.info("Canceling purchase order by ID: {}", id);
        PurchaseOrder order = findEntityById(id);

        if (order.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new BadRequestException("Ordens já recebidas não podem ser canceladas.");
        }

        if (order.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new BadRequestException("A ordem já está cancelada.");
        }

        // Verifica se há itens recebidos (mesmo parcialmente)
        boolean hasReceivedItems = order.getItems().stream()
                .anyMatch(item -> item.getReceivedQuantity().compareTo(BigDecimal.ZERO) > 0);

        if (hasReceivedItems) {
            throw new BadRequestException("Ordens com itens já recebidos não podem ser canceladas. Use devolução se necessário.");
        }

        PurchaseOrderStatus previousStatus = order.getStatus();
        order.setStatus(PurchaseOrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);
        logger.info("Purchase order ID: {} cancelled. Status changed from {} to CANCELLED.", id, previousStatus);

        PurchaseOrderResponseDTO response = toDto(savedOrder);
        addHateoasLinks(response);
        return response;
    }

    protected PurchaseOrder findEntityById(Long id) {
        logger.info("Finding purchase order by ID: {}", id);
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID: " + id));
    }

    private void validateItemsCanBeReplaced(PurchaseOrder order) {
        logger.warn("Validating if items can be replaced for purchase order ID: {}", order.getId_po());

        // Verifica se algum item foi parcialmente recebido
        boolean hasPartiallyReceivedItems = order.getItems().stream()
                .anyMatch(item -> item.getReceivedQuantity() != null &&
                        item.getReceivedQuantity().signum() > 0);

        if (hasPartiallyReceivedItems) {
            throw new BadRequestException(
                    "Não é possível substituir itens quando há recebimentos registrados. " +
                            "Itens com quantidade recebida não podem ser removidos. " +
                            "Para remover itens, cancele a ordem e crie uma nova."
            );
        }

        // Verifica status inconsistência (Safety check - não deve acontecer se a lógica está correta)
        if (order.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new BadRequestException(
                    "Estado inconsistente: ordem não está em DRAFT mas passou na validação inicial. " +
                            "Status atual: " + order.getStatus()
            );
        }

        // Verifica se há qualquer item com dados (quantidade não é zero)
        boolean hasAnyItemWithQuantity = order.getItems().stream()
                .anyMatch(item -> item.getQuantity() != null && item.getQuantity().signum() > 0);

        if (!hasAnyItemWithQuantity && !order.getItems().isEmpty()) {
            logger.warn("Warning: Order has items with zero quantity. This is unusual for DRAFT orders.");
        }

        logger.info("Items validation passed for purchase order ID: {}", order.getId_po());
    }

    private PurchaseOrder toEntity(PurchaseOrderRequestDTO dto) {
        List<PurchaseOrderItem> items = dto.getItems().stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + item.getProductId()));
                    return PurchaseOrderItem.builder()
                            .product(product)
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .build();
                })
                .toList();

        PurchaseOrder order = PurchaseOrder.builder()
                .supplierName(dto.getSupplierName())
                .notes(dto.getNotes())
                .items(items)
                .build();

        items.forEach(item -> item.setPurchaseOrder(order));

        return order;
    }

    private PurchaseOrderResponseDTO toDto(PurchaseOrder entity) {
        List<PurchaseOrderItemResponseDTO> itemsDto = entity.getItems().stream()
                .map(item -> PurchaseOrderItemResponseDTO.builder()
                        .id(item.getIdPoi())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubTotal())
                        .receivedQuantity(item.getReceivedQuantity())
                        .build())
                .toList();

        return PurchaseOrderResponseDTO.builder()
                .id(entity.getId_po())
                .supplierName(entity.getSupplierName())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .totalAmount(entity.getTotalAmount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .confirmedAt(entity.getConfirmedAt())
                .receivedAt(entity.getReceivedAt())
                .items(itemsDto)
                .build();
    }

    private void addHateoasLinks(PurchaseOrderResponseDTO dto) {
        dto.add(linkTo(methodOn(PurchaseOrderController.class).findAll(null, null, null, null, null, null, null, null, null, null, 0, 12, "asc")).withRel("findAll").withType("GET"));
        dto.add(linkTo(methodOn(PurchaseOrderController.class).create(null)).withRel("create").withType("POST"));
        dto.add(linkTo(methodOn(PurchaseOrderController.class).findById(dto.getId())).withSelfRel().withType("GET"));
        dto.add(linkTo(methodOn(PurchaseOrderController.class).update(dto.getId(), null)).withRel("update").withType("PUT"));
        dto.add(linkTo(methodOn(PurchaseOrderController.class).confirm(dto.getId())).withRel("confirm").withType("PATCH"));
        dto.add(linkTo(methodOn(PurchaseOrderController.class).receiveItems(dto.getId(), null)).withRel("receiveItems").withType("PATCH"));
        dto.add(linkTo(methodOn(PurchaseOrderController.class).cancel(dto.getId())).withRel("cancel").withType("PATCH"));
    }
}
