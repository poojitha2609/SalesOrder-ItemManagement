package org.example.demo.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.demo.dto.ItemDTO;
import org.example.demo.entity.OrderItem;
import org.example.demo.entity.SalesOrder;
import org.example.demo.repository.OrderItemRepository;
import org.example.demo.repository.SalesOrderRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SalesOrderService {
    private SalesOrderRepository salesOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WebClient.Builder webClientBuilder;


    public SalesOrderService(SalesOrderRepository salesOrderRepository, OrderItemRepository orderItemRepository, WebClient.Builder webClientBuilder) {
        this.salesOrderRepository = salesOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.webClientBuilder = webClientBuilder;
    }

    public String createOrder(SalesOrder order) {
        List<OrderItem> orderItems = order.getOrderItems();
        for (OrderItem orderItem : orderItems) {
            Long itemNo = orderItem.getItemNo();
            if (itemNo == null) {
                throw new IllegalArgumentException("ItemNo is missing in OrderItem.");
            }
          ItemDTO itemDTO =fetchItemDetails(orderItem.getItemNo());
            if (itemDTO == null) {
                throw new IllegalArgumentException("Item not found for ItemNo: " + itemNo);
            }
            if (!itemDTO.getItemNo().equals(orderItem.getItemNo())) {
                throw new IllegalArgumentException("ItemNo mismatch for OrderItem.");
            }

            if (itemDTO.getStockQty() < orderItem.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for ItemNo " + itemNo + ". Available: " + itemDTO.getStockQty());
            }

            orderItem.setPrice((double) itemDTO.getPrice());
            updateItemStockInItemManagement(itemDTO.getItemNo(), itemDTO.getStockQty() - orderItem.getQuantity());
            orderItem.setSalesOrder(order);
            order.setOrderItems(orderItems);
        }
        updateTotalAndFinalAmount(order);;
        salesOrderRepository.save(order);

        return "SalesOrder created successfully with ID: " + order.getOrderId();
    }

    public void updateItemStockInItemManagement(Long itemNo, int updatedStockQty) {
        webClientBuilder.baseUrl("http://localhost:8081")
            .build()
            .put()
            .uri(uriBuilder -> uriBuilder
                    .path("/items/{itemNo}/updateStock")
                    .build(itemNo))
            .bodyValue(Map.of("newStockQty", updatedStockQty))
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }


    public String addItemToOrder(Long orderId, OrderItem newItem) {
        SalesOrder salesOrder = salesOrderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        ItemDTO itemDTO = fetchItemDetails(newItem.getItemNo());
        Optional<OrderItem> existingItem = salesOrder.getOrderItems().stream()
                .filter(item -> item.getItemNo().equals(newItem.getItemNo()))
                .findFirst();

        if (existingItem.isPresent()) {
            OrderItem item = existingItem.get();
            int newQuantity = item.getQuantity() + newItem.getQuantity();
            if (newQuantity > itemDTO.getStockQty()) {
                throw new IllegalArgumentException();
            }
            item.setQuantity(newQuantity);
        } else {
            if (newItem.getQuantity() > itemDTO.getStockQty()) {
                throw new IllegalArgumentException();
            }

            newItem.setPrice((double) itemDTO.getPrice());
            newItem.setSalesOrder(salesOrder);
            salesOrder.getOrderItems().add(newItem);
        }
        updateItemStockInItemManagement(itemDTO.getItemNo(), itemDTO.getStockQty() - newItem.getQuantity());
        updateTotalAndFinalAmount(salesOrder);
        salesOrderRepository.save(salesOrder);
        return "Item added to SalesOrder successfully.";
    }

    public String deleteItemFromOrder(Long orderId, Long itemId, Integer quantityToRemove) {
            SalesOrder order = salesOrderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
            List<OrderItem> items = order.getOrderItems();
            OrderItem itemToDelete = items.stream()
                    .filter(item -> item.getItemNo().equals(itemId))
                    .findFirst()
                    .orElseThrow(EntityNotFoundException::new);
            if (quantityToRemove == null || quantityToRemove <= 0) {
                quantityToRemove = 1;
            }
            if (itemToDelete.getQuantity() > quantityToRemove) {
                itemToDelete.setQuantity(itemToDelete.getQuantity() - quantityToRemove);
                itemToDelete.setTotalPrice(itemToDelete.getPrice() * itemToDelete.getQuantity());
            } else {
                items.remove(itemToDelete);
                orderItemRepository.delete(itemToDelete);
            }
            for (OrderItem item : order.getOrderItems()) {
                item.setSalesOrder(order);
                orderItemRepository.save(item);
            }
            updateTotalAndFinalAmount(order);
            salesOrderRepository.save(order);
            return "Item with ID " + itemId + " successfully removed from SalesOrder " + orderId + ".";
    }

    public String applyDiscount(Long orderId, Double discount) {
        SalesOrder salesOrder = salesOrderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        salesOrder.setDiscount(discount);
        double totalAmount = salesOrder.getTotalAmount();
        double finalAmount = totalAmount - (totalAmount * (discount / 100));
        salesOrder.setFinalAmount(finalAmount);
        salesOrderRepository.save(salesOrder);
        return "Discount applied successfully to SalesOrder with ID: " + orderId;
    }

    public SalesOrder getSalesOrder(Long orderId) {
        SalesOrder salesOrder = salesOrderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        return salesOrder;
    }

    public String getSalesOrderWithItemDetails(Long orderId, Long itemId) {
        Optional<SalesOrder> salesOrderOptional = Optional.ofNullable(salesOrderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new));
        SalesOrder salesOrder = salesOrderOptional.get();
        String itemDetails = webClientBuilder.baseUrl("http://localhost:8081")
                .build()
                .get()
                .uri("/items/" + itemId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return "SalesOrder: " + salesOrder + "\nItem Details: " + itemDetails;
    }


    public String deleteSalesOrder(Long orderId) {
        salesOrderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        salesOrderRepository.deleteById(orderId);
        return "Sales Order "+orderId+" Deleted successfully";
    }


    private void updateTotalAndFinalAmount(SalesOrder order) {
        double totalAmount = 0;
        for (OrderItem item : order.getOrderItems()) {
            totalAmount += item.getTotalPrice();
        }
        order.setTotalAmount(totalAmount);
        double discount = order.getDiscount();
        double finalAmount = totalAmount - (totalAmount * (discount / 100));
        order.setFinalAmount(finalAmount);
    }

    private ItemDTO fetchItemDetails(Long itemNo) {
        return webClientBuilder.baseUrl("http://localhost:8081")
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder.path("/items/{itemNo}").build(Map.of("itemNo", itemNo)))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ItemDTO.class)
                .blockOptional()
                .orElseThrow(IllegalArgumentException::new);
    }


}
