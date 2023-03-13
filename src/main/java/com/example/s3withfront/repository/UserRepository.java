package com.example.s3withfront.repository;

import com.example.s3withfront.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
