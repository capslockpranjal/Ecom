package org.example.zenvybackend.category.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.zenvybackend.common.entity.BaseEntity;

@Entity
@Table(name = "category_metadata_field_values")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryMetadataFieldValues extends BaseEntity {

    @EmbeddedId
    private CategoryMetadataFieldValuesId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryId")
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryMetadataFieldId")
    @JoinColumn(name = "category_metadata_field_id", nullable = false)
    private CategoryMetadataField field;

    @Column(nullable = false)
    private String metadataValues;
}