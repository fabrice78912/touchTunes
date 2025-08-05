package com.example.producer.service.nosql;

import com.example.producer.model.User;
import com.example.producer.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public User createUser(User user) {
    user.setCreatedAt(java.time.Instant.now());
    return userRepository.save(user);
  }
}
