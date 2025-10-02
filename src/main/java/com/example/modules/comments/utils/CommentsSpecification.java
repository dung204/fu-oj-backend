package com.example.modules.comments.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.comments.entities.Comment;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentsSpecification extends SpecificationBuilder<Comment> {

  public static CommentsSpecification builder() {
    return new CommentsSpecification();
  }
}
