package ru.t1.authservice.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Table(name = "refresh_tokens")
@Entity
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public class RefreshTokenEntity {

    @Id
    private String id;
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;
}
