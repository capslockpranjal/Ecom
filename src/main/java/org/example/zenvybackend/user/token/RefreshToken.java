package org.example.zenvybackend.user.token;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.zenvybackend.common.entity.BaseEntity;
import org.example.zenvybackend.user.entity.User;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
public class RefreshToken extends BaseEntity {



    @Column(nullable = false, unique = true)
    private String token;

    private LocalDateTime expiryDate;

    @ManyToOne
    private User user;
}
