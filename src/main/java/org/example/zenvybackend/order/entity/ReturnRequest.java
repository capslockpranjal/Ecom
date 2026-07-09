package org.example.zenvybackend.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.zenvybackend.common.entity.BaseEntity;
import org.example.zenvybackend.order.enums.ReturnStatus;
import org.example.zenvybackend.user.entity.Customer;

import java.util.UUID;

@Entity
@Table(name = "return_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnRequest extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_user_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_order_id", nullable = false)
    private SellerOrder sellerOrder;

    @Column(nullable = false)
    private Integer quantity;

    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnStatus status;
}
