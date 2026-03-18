package com.example.userservice.service;

import com.example.userservice.entity.User;

public interface UserService {
    User register(String username, String password);
    User login(String username, String password);
    User getByToken(String token);
}

