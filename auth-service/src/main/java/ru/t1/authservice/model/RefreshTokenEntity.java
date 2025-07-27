package ru.t1.authservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table(name = "refresh_tokens")
@Entity
@Setter
@Getter
public class RefreshTokenEntity {

    @Id
    private String id;
    private String refreshToken;
}
