package org.example.zenvybackend.product.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "category_metadata_field_values")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryMetadataFieldValues {

    @EmbeddedId
    private CategoryMetadataFieldValuesId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryId")
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryMetadataFieldId")
    @JoinColumn(name = "category_metadata_field_id")
    private CategoryMetadataField field;

    @Column(nullable = false)
    private String metadataValues;

}