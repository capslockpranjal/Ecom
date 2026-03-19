package org.example.zenvybackend.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.zenvybackend.common.entity.BaseEntity;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariation extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Integer quantityAvailable;

    private Double price;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    private String primaryCategoryName;

    private Boolean isActive = true;
}
