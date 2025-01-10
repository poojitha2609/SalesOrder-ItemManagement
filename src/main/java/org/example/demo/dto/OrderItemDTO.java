package org.example.demo.dto;

import lombok.*;
import org.example.demo.entity.SalesOrder;

@Getter
@Setter
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long itemNo;
    private String description;
    private Double price;
    private Integer quantity;
    private SalesOrder salesOrder;
}
