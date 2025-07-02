package com.animation.generator.objects;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String email;
    private String username;
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
