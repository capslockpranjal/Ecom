package org.example.zenvybackend.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.zenvybackend.category.entity.Category;
import org.example.zenvybackend.common.entity.BaseEntity;
import org.example.zenvybackend.user.entity.Seller;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Entity
@Table(
        name = "product",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"name", "brand", "category_id", "seller_user_id"}
                )
        }
)
@SQLDelete(sql = "UPDATE product SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
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

    @Column(name = "is_cancellable",nullable = false)
    private Boolean isCancellable;

    @Column(name = "is_returnable",nullable = false)
    private Boolean isReturnable;

    @Column(nullable = false)
    private String brand;

    @Column(name = "is_active",nullable = false)
    private Boolean isActive= false;
}