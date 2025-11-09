package com.example.modules.courses.repositories;

import com.example.modules.courses.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CoursesRepository
  extends JpaRepository<Course, String>, JpaSpecificationExecutor<Course> {}
