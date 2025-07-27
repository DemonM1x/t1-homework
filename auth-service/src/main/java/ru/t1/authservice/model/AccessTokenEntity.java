package ru.t1.authservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Indexed;

@Table(name = "access_tokens")
@Entity
@Setter
@Getter
public class AccessTokenEntity {

    @Id
    private String id;
    private String accessToken;
}
