package com.animation.generator.objects;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "guest_id", unique = true)
    private String guestId;
    private String email;
    private String username;
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
