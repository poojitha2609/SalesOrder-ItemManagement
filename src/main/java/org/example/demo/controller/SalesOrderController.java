package org.example.demo.controller;

import org.example.demo.dto.SalesOrderDTO;
import org.example.demo.entity.OrderItem;
import org.example.demo.entity.SalesOrder;
import org.example.demo.repository.SalesOrderRepository;
import org.example.demo.service.SalesOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

@RestController
@RequestMapping("/sales-orders")
public class SalesOrderController {
    private final SalesOrderService salesOrderService;
    private final SalesOrderRepository salesOrderRepository;

    public SalesOrderController(SalesOrderService salesOrderService, SalesOrderRepository salesOrderRepository) {
        this.salesOrderService = salesOrderService;
        this.salesOrderRepository = salesOrderRepository;
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody SalesOrderDTO salesOrderDTO) {
            SalesOrder salesOrder = new SalesOrder(salesOrderDTO);
            String createdOrder = salesOrderService.createOrder(salesOrder);
            return ResponseEntity.ok(createdOrder);
    }


    @PostMapping("/{orderId}")
    public ResponseEntity<String> addItemToOrder(@PathVariable Long orderId, @RequestBody OrderItem orderItem) {
        String updatedOrder = salesOrderService.addItemToOrder(orderId, orderItem);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{orderId}/{itemId}")
    public ResponseEntity<String> deleteItemWithoutQuantity(
            @PathVariable Long orderId,
            @PathVariable Long itemId) {
        String updatedOrder = salesOrderService.deleteItemFromOrder(orderId, itemId, null);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{orderId}/{itemId}/{quantityToRemove}")
    public ResponseEntity<String> deleteItemWithQuantity(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @PathVariable Integer quantityToRemove) {
        String updatedOrder = salesOrderService.deleteItemFromOrder(orderId, itemId, quantityToRemove);
        return ResponseEntity.ok(updatedOrder);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<String> applyDiscount(
            @PathVariable Long orderId,
            @RequestBody Map<String, Double> requestBody) {
        Double discount = requestBody.get("discount");
        if (discount == null || discount < 0) {
            return ResponseEntity.badRequest().body("Invalid discount value provided.");
        }
        salesOrderService.applyDiscount(orderId, discount);
        return ResponseEntity.ok("Discount of " + discount + "% applied successfully to order ID " + orderId + "!");
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<SalesOrder> getSalesOrder(@PathVariable Long orderId) {
        SalesOrder salesOrder = salesOrderService.getSalesOrder(orderId);
        return ResponseEntity.ok(salesOrder);
    }


    @GetMapping("/{orderId}/items/{itemId}")
    public String getSalesOrderWithItemDetails(@PathVariable Long orderId, @PathVariable Long itemId) {
        return salesOrderService.getSalesOrderWithItemDetails(orderId, itemId);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteSalesOrder(@PathVariable Long orderId) {
        String deleteOrder = salesOrderService.deleteSalesOrder(orderId);
        return ResponseEntity.ok(deleteOrder);
    }

    @GetMapping("item/{itemNo}")
    public boolean isItemInOrder(@PathVariable Long itemNo) {
        return salesOrderRepository.existsByOrderItems_ItemNo(itemNo);
    }


}
