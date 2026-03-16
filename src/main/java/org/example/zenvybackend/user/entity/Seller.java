package org.example.zenvybackend.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.zenvybackend.common.entity.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "seller")
@Getter @Setter
public class Seller extends BaseEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(unique = true, nullable = false)
    private String gst;

    @Column(nullable = false,unique = true)
    private String companyName;

    @Column(nullable = false)
    private String companyContact;


}
