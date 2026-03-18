package com.example.userservice.controller;

import com.example.userservice.entity.User;
import com.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> req) {
        User user = userService.register(req.get("username"), req.get("password"));
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 0);
        resp.put("message", "success");
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        resp.put("data", data);
        return resp;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> req) {
        User user = userService.login(req.get("username"), req.get("password"));
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 0);
        resp.put("message", "success");
        Map<String, Object> data = new HashMap<>();
        data.put("token", user.getSalt()); // 临时用salt字段存token
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        resp.put("data", data);
        return resp;
    }

    @GetMapping("/me")
    public Map<String, Object> me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        User user = userService.getByToken(token);
        Map<String, Object> resp = new HashMap<>();
        if (user == null) {
            resp.put("code", 401);
            resp.put("message", "unauthorized");
            return resp;
        }
        resp.put("code", 0);
        resp.put("message", "success");
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("createdAt", user.getCreatedAt());
        resp.put("data", data);
        return resp;
    }
}

