package com.animation.generator.dtos;

import lombok.Data;

@Data
public class UserRequest {
    private Long userId;
    private Long chatId;
    private String prompt;
}
