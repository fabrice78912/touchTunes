package com.example.producer.web;

import com.example.producer.mapper.UserMapper;
import com.example.producer.model.User;
import com.example.producer.service.nosql.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final UserMapper userMapper;

  @PostMapping
  public ResponseEntity<User> createUser(@RequestBody User user) {
    User createdUser = userService.createUser(user);
    return ResponseEntity.ok(createdUser);
  }
}
