package ru.t1.authservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.stereotype.Indexed;

import java.util.List;

@Table(name = "access_tokens")
@Entity
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public class AccessTokenEntity {

    @Id
    private String id;
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "access_token", columnDefinition = "TEXT")
    private List<String> accessToken;
}
