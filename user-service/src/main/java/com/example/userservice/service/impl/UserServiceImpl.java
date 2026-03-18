package com.example.userservice.service.impl;

import com.example.userservice.entity.User;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public User register(String username, String password) {
        if (userMapper.findByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }
        String salt = UUID.randomUUID().toString().replaceAll("-", "");
        String passwordHash = DigestUtils.md5DigestAsHex((password + salt).getBytes(StandardCharsets.UTF_8));
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setSalt(salt);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setStatus(1);
        userMapper.insertUser(user);
        return userMapper.findByUsername(username);
    }

    @Override
    @Cacheable(value = "user", key = "#username")
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) throw new RuntimeException("用户不存在");
        String hash = DigestUtils.md5DigestAsHex((password + user.getSalt()).getBytes(StandardCharsets.UTF_8));
        if (!hash.equals(user.getPasswordHash())) throw new RuntimeException("密码错误");
        // 生成token
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("token:" + token, user.getId(), 3600); // 1小时过期
        // 临时将token存到user对象（仅演示，实际应返回token）
        user.setSalt(token);
        return user;
    }

    @Override
    public User getByToken(String token) {
        Long userId = (Long) redisTemplate.opsForValue().get("token:" + token);
        if (userId == null) return null;
        return userMapper.findById(userId);
    }
}
