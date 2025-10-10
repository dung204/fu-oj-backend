package com.example.modules.comments.repositories;

import com.example.modules.comments.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CommentsRepository
  extends JpaRepository<Comment, String>, JpaSpecificationExecutor<Comment> {}
