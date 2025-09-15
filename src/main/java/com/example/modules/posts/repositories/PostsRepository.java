package com.example.modules.posts.repositories;

import com.example.modules.posts.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PostsRepository
  extends JpaRepository<Post, String>, JpaSpecificationExecutor<Post> {}
