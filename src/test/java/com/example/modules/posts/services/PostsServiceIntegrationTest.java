package com.example.modules.posts.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.example.base.BaseServiceIntegrationTest;
import com.example.modules.posts.dtos.CreatePostDTO;
import com.example.modules.posts.dtos.MePostsSearchDTO;
import com.example.modules.posts.dtos.PostResponseDTO;
import com.example.modules.posts.dtos.PostsSearchDTO;
import com.example.modules.posts.dtos.UpdatePostDTO;
import com.example.modules.posts.entities.Post;
import com.example.modules.posts.exceptions.PostNotFoundException;
import com.example.modules.posts.repositories.PostsRepository;
import com.example.modules.users.entities.User;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

public class PostsServiceIntegrationTest extends BaseServiceIntegrationTest {

  @Autowired
  private PostsService postsService;

  @Autowired
  private PostsRepository postsRepository;

  private Post publicPost;
  private Post privatePost;
  private Post deletedPublicPost;

  @BeforeEach
  @Override
  protected void setup() {
    super.setup();
    User user = getUser();

    publicPost = Post.builder()
      .title("Spring Boot Guide")
      .content("Content A")
      .isPublic(true)
      .user(user)
      .build();

    privatePost = Post.builder()
      .title("Private Post")
      .content("Content B")
      .isPublic(false)
      .user(user)
      .build();

    deletedPublicPost = Post.builder()
      .title("Deleted Public")
      .content("Content C")
      .isPublic(true)
      .user(user)
      .deletedTimestamp(Instant.now())
      .build();

    postsRepository.saveAll(List.of(publicPost, privatePost, deletedPublicPost));
  }

  @AfterEach
  @Override
  protected void cleanup() {
    postsRepository.deleteAll();
    postsRepository.flush();
    super.cleanup();
  }

  @Test
  void findAllPublicPosts_ShouldReturnOnlyPublicAndDoNotReturnDeletedOrPrivatePosts() {
    PostsSearchDTO searchDTO = new PostsSearchDTO();

    Page<PostResponseDTO> result = postsService.findAllPublicPosts(searchDTO);
    assertEquals(1, result.getTotalElements());

    PostResponseDTO post = result.getContent().get(0);
    assertTrue(post.getIsPublic());
  }

  @Test
  void findAllPublicPosts_WhenTitleDoesNotMatchAnyPost_ShouldReturnEmpty() {
    PostsSearchDTO searchDTO = new PostsSearchDTO();
    searchDTO.setTitle("Nonexistent");

    Page<PostResponseDTO> result = postsService.findAllPublicPosts(searchDTO);
    assertEquals(0, result.getTotalElements());
  }

  @Test
  void findAllPublicPosts_WhenTitleMatchesAnyPublicPosts_ShouldReturnPosts() {
    PostsSearchDTO searchDTO = new PostsSearchDTO();
    searchDTO.setTitle("spring bo");

    Page<PostResponseDTO> result = postsService.findAllPublicPosts(searchDTO);
    assertEquals(1, result.getTotalElements());

    PostResponseDTO post = result.getContent().get(0);
    assertTrue(post.getIsPublic());
  }

  @Test
  void findAllPublicPosts_WhenOwnedByMatchesAnyPublicPosts_ShouldReturnPosts() {
    User user = getUser();
    PostsSearchDTO searchDTO = new PostsSearchDTO();
    searchDTO.setUser(user.getId());

    Page<PostResponseDTO> result = postsService.findAllPublicPosts(searchDTO);
    assertEquals(1, result.getTotalElements());

    PostResponseDTO post = result.getContent().get(0);
    assertTrue(post.getIsPublic());
  }

  @Test
  void findAllPublicPosts_WhenOwnedByDoesNotMatchAnyPost_ShouldReturnEmpty() {
    PostsSearchDTO searchDTO = new PostsSearchDTO();
    searchDTO.setUser("unknown");

    Page<PostResponseDTO> result = postsService.findAllPublicPosts(searchDTO);
    assertEquals(0, result.getTotalElements());
  }

  @Test
  void findAllPostsOfCurrentUser_ShouldReturnAllPostsOfTheUser() {
    User user = getUser();
    MePostsSearchDTO searchDTO = new MePostsSearchDTO();

    Page<PostResponseDTO> result = postsService.findAllPostsOfCurrentUser(searchDTO, user);
    assertEquals(3, result.getTotalElements());
  }

  @Test
  void findAllPostsOfCurrentUser_WhenTitleDoesNotMatchAnyPost_ShouldReturnEmpty() {
    User user = getUser();
    MePostsSearchDTO searchDTO = new MePostsSearchDTO();
    searchDTO.setName("not match");

    Page<PostResponseDTO> result = postsService.findAllPostsOfCurrentUser(searchDTO, user);
    assertEquals(0, result.getTotalElements());
  }

  @Test
  void findPostById_WhenCurrentUserNotExistAndPublicPostWithIdExists_ShouldReturnPost() {
    String postId = publicPost.getId();
    PostResponseDTO result = postsService.findPostById(postId, null);
    assertEquals(postId, result.getId());
  }

  @Test
  void findPostById_WhenCurrentUserNotExistAndPublicPostWithIdNotExist_ShouldThrowPostNotFoundException() {
    String postId = "not-found";
    assertThrows(PostNotFoundException.class, () -> postsService.findPostById(postId, null));
  }

  @Test
  void findPostById_WhenCurrentUserNotExistAndPrivatePostWithIdExists_ShouldThrowPostNotFoundException() {
    String postId = privatePost.getId();
    assertThrows(PostNotFoundException.class, () -> postsService.findPostById(postId, null));
  }

  @Test
  void findPostById_WhenCurrentUserNotExistAndDeletedPostWithIdExists_ShouldThrowPostNotFoundException() {
    String postId = deletedPublicPost.getId();
    assertThrows(PostNotFoundException.class, () -> postsService.findPostById(postId, null));
  }

  @Test
  void findPostById_WhenCurrentUserExistsAndPostOfTheCurrentUserWithIdExists_ShouldReturnPost() {
    User user = getUser();
    assertEquals(publicPost.getId(), postsService.findPostById(publicPost.getId(), user).getId());
    assertEquals(privatePost.getId(), postsService.findPostById(privatePost.getId(), user).getId());
    assertEquals(
      deletedPublicPost.getId(),
      postsService.findPostById(deletedPublicPost.getId(), user).getId()
    );
  }

  @Test
  void createPost_ShouldSaveAndReturnMappedPost() {
    User user = getUser();
    CreatePostDTO request = CreatePostDTO.builder()
      .title("New Public Post")
      .content("Content")
      .isPublic(JsonNullable.of(true))
      .build();

    PostResponseDTO post = postsService.createPost(request, user);

    assertNotNull(post);
    assertEquals(request.getTitle(), post.getTitle());
    assertEquals(request.getContent(), post.getContent());
    assertEquals(request.getIsPublic().get(), post.getIsPublic());
    assertEquals(user.getId(), post.getUser().getId());
  }

  @Test
  void createPost_WhenIsPublicIsNotPresent_ShouldSaveAndReturnMappedPostWithIsPublicIsFalse() {
    User user = getUser();
    CreatePostDTO request = CreatePostDTO.builder()
      .title("New Private Post")
      .content("Content")
      .build();

    PostResponseDTO post = postsService.createPost(request, user);

    assertNotNull(post);
    assertEquals(request.getTitle(), post.getTitle());
    assertEquals(request.getContent(), post.getContent());
    assertFalse(post.getIsPublic());
    assertEquals(user.getId(), post.getUser().getId());
  }

  @Test
  void updatePost_WhenPostExists_ShouldAssignAndSaveAndReturnMappedPost() {
    User user = getUser();
    String postId = publicPost.getId();
    UpdatePostDTO updatePostDTO = UpdatePostDTO.builder()
      .title(JsonNullable.of("Updated title"))
      .content(JsonNullable.of("Update content"))
      .isPublic(JsonNullable.of(false))
      .build();

    PostResponseDTO postResponseDTO = postsService.updatePost(postId, updatePostDTO, user);

    assertNotNull(postResponseDTO);
    assertEquals(updatePostDTO.getTitle().get(), postResponseDTO.getTitle());
    assertEquals(updatePostDTO.getContent().get(), postResponseDTO.getContent());
    assertEquals(updatePostDTO.getIsPublic().get(), postResponseDTO.getIsPublic());
  }

  @Test
  void updatePost_WhenPostDoesNotExist_ShouldThrowPostNotFoundException() {
    String postId = "not-found";
    User user = getUser();
    UpdatePostDTO updatePostDTO = UpdatePostDTO.builder().build();

    assertThrows(PostNotFoundException.class, () ->
      postsService.updatePost(postId, updatePostDTO, user)
    );
  }

  @Test
  void deletePost_WhenPostExists_ShouldSetDeletedTimestampAndSave() {
    String postId = publicPost.getId();
    User user = getUser();

    postsService.deletePost(postId, user);
    Post savedPost = postsRepository.findById(postId).get();

    assertNotEquals(null, savedPost);
  }

  @Test
  void deletePost_WhenPostDoesNotExist_ShouldThrowPostNotFoundException() {
    String postId = "not-found";
    User user = getUser();

    assertThrows(PostNotFoundException.class, () -> postsService.deletePost(postId, user));
  }

  @Test
  void restorePost_WhenDeletedPostExists_ShouldRestoreAndReturnMappedPost() {
    String postId = deletedPublicPost.getId();
    User user = getUser();

    PostResponseDTO post = postsService.restorePost(postId, user);

    assertNotNull(post);
    assertEquals(null, post.getDeletedTimestamp());
  }

  @Test
  void restorePost_WhenDeletedPostDoesNotExist_ShouldThrowPostNotFoundException() {
    String postId = publicPost.getId();
    User user = getUser();

    assertThrows(PostNotFoundException.class, () -> postsService.restorePost(postId, user));
  }
}
