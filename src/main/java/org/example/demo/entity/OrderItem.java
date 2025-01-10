package org.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo.dto.OrderItemDTO;

@Entity
@Table(name = "order_item")
@Data
@NoArgsConstructor
public class OrderItem {

    @Id
    @Column(name = "item_no", nullable = false)
    @NotNull
    private Long itemNo;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;


    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinColumn(name = "sales_order_id", referencedColumnName = "order_id")
    private SalesOrder salesOrder;

    public OrderItem(OrderItemDTO orderItemDTO) {
        this.itemNo = orderItemDTO.getItemNo();
        this.description = orderItemDTO.getDescription();
        this.price = orderItemDTO.getPrice();
        this.quantity = orderItemDTO.getQuantity();
    }


    public double getTotalPrice() {
        return (this.price != null && this.quantity != null) ? this.price * this.quantity : 0.0;
    }

    public void setTotalPrice(double v) {
        this.price = v;
    }
}
