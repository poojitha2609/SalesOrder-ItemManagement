package org.example.demo.dto;


import lombok.*;
import org.example.demo.entity.OrderItem;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SalesOrderDTO {
    private Long orderId;
    private Date orderDate;
    private String customerName;
    private Double discount;
    private Double totalAmount;
    private Double finalAmount;
    private List<OrderItem> orderItems;

}
