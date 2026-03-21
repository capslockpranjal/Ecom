package org.example.zenvybackend.category.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.zenvybackend.common.entity.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Category parentCategory;
}
