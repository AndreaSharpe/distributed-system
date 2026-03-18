package com.example.userservice.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private String salt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer status;
}

