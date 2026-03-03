package org.example.zenvybackend.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.zenvybackend.common.entity.BaseEntity;

@Entity
@Getter
@Setter
public class Role extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String authority;
}
