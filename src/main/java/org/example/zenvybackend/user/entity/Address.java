package org.example.zenvybackend.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.zenvybackend.common.entity.BaseEntity;

@Entity
@Getter
@Setter
public class Address extends BaseEntity {

    private String city;
    private String state;
    private String country;
    private String addressLine;
    private String zipCode;
    private String label;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
