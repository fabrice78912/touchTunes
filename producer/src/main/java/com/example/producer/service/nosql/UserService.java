package com.example.producer.service.nosql;

import com.example.producer.model.User;
import com.example.producer.repo.UserRepository;
import com.example.common_lib.model.exception.DuplicateEmailException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public User createUser(User user) {
      // Vérifie si un utilisateur avec le même email existe déjà
      if (userRepository.findByEmail(user.getEmail()).isPresent()) {
        throw new DuplicateEmailException("L'email existe déjà : " + user.getEmail());
      }

      user.setCreatedAt(java.time.Instant.now());
      return userRepository.save(user);
  }
}
