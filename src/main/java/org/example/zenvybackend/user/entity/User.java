package org.example.zenvybackend.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.zenvybackend.common.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String middleName;
    private String lastName;

    private Boolean isActive = false;
    private Boolean isExpired = false;
    @Column(name = "is_locked")
    private Boolean isLocked = false;

    private Integer invalidAttemptCount = 0;

    private LocalDateTime passwordUpdateDate;
    private LocalDateTime passwordExpiryDate;
    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}
