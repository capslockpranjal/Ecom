package org.example.zenvybackend.product.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CategoryMetadataFieldValuesId implements Serializable {

    private UUID categoryId;

    private UUID categoryMetadataFieldId;

}
