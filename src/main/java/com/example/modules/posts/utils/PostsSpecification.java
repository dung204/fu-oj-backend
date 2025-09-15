package com.example.modules.posts.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.posts.entities.Post;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostsSpecification extends SpecificationBuilder<Post> {

  public static PostsSpecification builder() {
    return new PostsSpecification();
  }

  public PostsSpecification containsTitle(String title) {
    if (title != null && !title.trim().isEmpty()) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.like(
          criteriaBuilder.lower(root.get("title")),
          "%" + title.toLowerCase() + "%"
        )
      );
    }
    return this;
  }

  public PostsSpecification publicOnly() {
    specifications.add((root, query, criteriaBuilder) ->
      criteriaBuilder.isTrue(root.get("isPublic"))
    );
    return this;
  }

  public PostsSpecification publicOrOwnedBy(String userId) {
    specifications.add((root, query, criteriaBuilder) ->
      criteriaBuilder.or(
        criteriaBuilder.isTrue(root.get("isPublic")),
        criteriaBuilder.equal(root.get("user").get("id"), userId)
      )
    );
    return this;
  }

  public PostsSpecification ownedBy(String userId) {
    if (userId != null && !userId.trim().isEmpty()) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("user").get("id"), userId)
      );
    }
    return this;
  }

  public PostsSpecification createdAfter(java.time.LocalDateTime date) {
    if (date != null) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.greaterThanOrEqualTo(root.get("createdTimestamp"), date)
      );
    }
    return this;
  }

  public PostsSpecification createdBefore(java.time.LocalDateTime date) {
    if (date != null) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.lessThanOrEqualTo(root.get("createdTimestamp"), date)
      );
    }
    return this;
  }
}
