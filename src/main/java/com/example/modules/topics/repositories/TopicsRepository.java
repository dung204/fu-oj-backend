package com.example.modules.topics.repositories;

import com.example.modules.topics.entities.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicsRepository
  extends JpaRepository<Topic, String>, JpaSpecificationExecutor<Topic> {}
