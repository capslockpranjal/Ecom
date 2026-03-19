package org.example.zenvybackend.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.zenvybackend.common.entity.BaseEntity;
import org.example.zenvybackend.user.entity.Seller;

import java.util.UUID;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    /* SELLER MAPPING */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_user_id", nullable = false)
    private Seller seller;

    /* CATEGORY MAPPING */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "is_cancellable")
    private Boolean isCancellable;

    @Column(name = "is_returnable")
    private Boolean isReturnable;

    private String brand;

    @Column(name = "primary_image_name")
    private String primaryImageName;

    @Column(name = "is_active")
    private Boolean isActive;
}