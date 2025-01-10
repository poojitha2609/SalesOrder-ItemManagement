package org.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.demo.dto.SalesOrderDTO;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "sales_order")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SalesOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_date")
    private Date orderDate;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "discount")
    private Double discount;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "final_amount")
    private Double finalAmount;

   @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;



    public SalesOrder(SalesOrderDTO dto) {
        this.orderId = dto.getOrderId();
        this.orderDate = dto.getOrderDate();
        this.customerName = dto.getCustomerName();
        this.discount = dto.getDiscount();
        this.orderItems = dto.getOrderItems();
    }
}