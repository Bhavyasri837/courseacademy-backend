package com.courseacademy.repository;

import com.courseacademy.entity.LoginLockout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginLockoutRepository extends JpaRepository<LoginLockout, Long> {
    Optional<LoginLockout> findByKey(String key);
}

