package org.example.zenvybackend.user.token;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import lombok.*;
import org.example.zenvybackend.common.entity.BaseEntity;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedToken extends BaseEntity {



    @Column(nullable = false, unique = true)
    private String token;

    private LocalDateTime expiryDate;
}
