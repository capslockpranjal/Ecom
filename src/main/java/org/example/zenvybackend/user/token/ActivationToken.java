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
public class ActivationToken extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String token;

    private LocalDateTime expiryDate;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
