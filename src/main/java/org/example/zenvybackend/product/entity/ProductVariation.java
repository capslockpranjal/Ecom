package org.example.zenvybackend.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.zenvybackend.common.entity.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Entity
@Table(
        name = "product_variation",
        indexes = {
                @Index(name = "idx_product_id", columnList = "product_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_id", "metadata"})
        }
)
@SQLDelete(sql = "UPDATE product_variation SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariation extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantityAvailable;

    @Column(nullable = false)
    private Double price;


    @Column(columnDefinition = "json", nullable = false)
    private String metadata;
    @Column(nullable = false)
    private Boolean isActive = true;

    @Version
    private Long version;
}
