package com.campusmarket.backend.college.repository;

import com.campusmarket.backend.college.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CollegeRepository extends JpaRepository<College, Long> {
    Optional<College> findByEmailDomain(String emailDomain);
}