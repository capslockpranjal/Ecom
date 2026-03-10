package org.example.zenvybackend.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.zenvybackend.common.entity.BaseEntity;

@Entity
@Getter
@Setter
public class Customer extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String contact;
}
