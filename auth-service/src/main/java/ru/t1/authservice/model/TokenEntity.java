package ru.t1.authservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table(name = "tokens")
@Entity
@Setter
@Getter
public class TokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    private TokenType type = TokenType.BEARER;

    private boolean expired;

    private boolean revoked;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
