package com.animation.generator.objects;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity(name = "diagram")
@Data
public class Diagram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String prompt;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "chat_id")
    private Long chatId;
    @Column(name = "json_representation")
    private String jsonRepresentation;
    @Column(name = "generated_code")
    private String generatedCode;
    @Column(name = "video_source")
    private String videoSource;
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
