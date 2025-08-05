package com.example.producer.repo;

import com.example.producer.model.User;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

  Optional<User> findById(String jukeboxId);

  Optional<User> findByEmail(String email);

  // ✅ Requête MongoDB native équivalente à :)
/*  @Query("{ 'email': ?0 }")
  Optional<User> findByEmail(String email);*/
}
