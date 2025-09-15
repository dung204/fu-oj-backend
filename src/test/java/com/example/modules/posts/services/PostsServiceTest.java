package com.example.modules.posts.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.base.BaseServiceTest;
import com.example.modules.posts.dtos.CreatePostDTO;
import com.example.modules.posts.dtos.PostResponseDTO;
import com.example.modules.posts.dtos.PostsSearchDTO;
import com.example.modules.posts.dtos.UpdatePostDTO;
import com.example.modules.posts.entities.Post;
import com.example.modules.posts.exceptions.PostNotFoundException;
import com.example.modules.posts.repositories.PostsRepository;
import com.example.modules.posts.utils.PostMapper;
import com.example.modules.users.entities.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

public class PostsServiceTest extends BaseServiceTest {

  @Mock
  private PostsRepository postsRepository;

  @Mock
  private PostMapper postMapper;

  @InjectMocks
  private PostsService postsService;

  @Test
  void findAllPublicPosts_ShouldReturnMappedPage() {
    PostsSearchDTO searchDTO = mock(PostsSearchDTO.class);
    PageRequest pageRequest = mock(PageRequest.class);

    when(searchDTO.getTitle()).thenReturn(null);
    when(searchDTO.getUser()).thenReturn(null);
    when(searchDTO.toPageRequest()).thenReturn(pageRequest);

    Post post = Post.builder().title("test").build();
    Page<Post> postPage = new PageImpl<>(List.of(post));
    when(postsRepository.findAll(any(Specification.class), eq(pageRequest))).thenReturn(postPage);

    PostResponseDTO responseDTO = PostResponseDTO.builder().title(post.getTitle()).build();
    when(postMapper.toPostResponseDTO(post)).thenReturn(responseDTO);

    Page<PostResponseDTO> result = postsService.findAllPublicPosts(searchDTO);

    assertEquals(1, result.getTotalElements());
    assertEquals(responseDTO, result.getContent().get(0));
  }

  @Test
  void findAllPublicPosts_ShouldReturnEmptyResult() {
    PostsSearchDTO searchDTO = mock(PostsSearchDTO.class);
    PageRequest pageRequest = mock(PageRequest.class);

    when(searchDTO.getTitle()).thenReturn("no post with this name");
    when(searchDTO.getUser()).thenReturn("no post with this user");
    when(searchDTO.toPageRequest()).thenReturn(pageRequest);

    Page<Post> postPage = new PageImpl<>(List.of());
    when(postsRepository.findAll(any(Specification.class), eq(pageRequest))).thenReturn(postPage);

    Page<PostResponseDTO> result = postsService.findAllPublicPosts(searchDTO);

    assertTrue(result.isEmpty());
  }

  @Test
  void findPostById_WhenPublicAndNoUser_ShouldReturnMappedPost() {
    String postId = "post-id";
    Post post = Post.builder().id(postId).title("Public Post").build();
    PostResponseDTO responseDTO = PostResponseDTO.builder().title("Public Post").build();

    when(postsRepository.findOne(any(Specification.class))).thenReturn(Optional.of(post));
    when(postMapper.toPostResponseDTO(post)).thenReturn(responseDTO);

    PostResponseDTO result = postsService.findPostById(postId, null);

    assertEquals(responseDTO, result);
    verify(postsRepository).findOne(any(Specification.class));
    verify(postMapper).toPostResponseDTO(post);
  }

  @Test
  void findPostById_WhenUserIsOwner_ShouldReturnMappedPost() {
    String postId = "post-id";
    User user = mock(User.class);
    when(user.getId()).thenReturn("user-id");
    Post post = Post.builder().id(postId).title("User's Post").user(user).build();
    PostResponseDTO responseDTO = PostResponseDTO.builder().title("User's Post").build();

    when(postsRepository.findOne(any(Specification.class))).thenReturn(Optional.of(post));
    when(postMapper.toPostResponseDTO(post)).thenReturn(responseDTO);

    PostResponseDTO result = postsService.findPostById(postId, user);

    assertEquals(responseDTO, result);
    verify(postsRepository).findOne(any(Specification.class));
    verify(postMapper).toPostResponseDTO(post);
  }

  @Test
  void findPostById_WhenPostNotFound_ShouldThrowException() {
    String postId = "missing-id";
    User user = mock(User.class);
    when(user.getId()).thenReturn("user-id");

    when(postsRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

    assertThrows(PostNotFoundException.class, () -> postsService.findPostById(postId, user));
    verify(postsRepository).findOne(any(Specification.class));
    verifyNoInteractions(postMapper);
  }

  @Test
  void createPost_ShouldSaveAndReturnMappedPost() {
    CreatePostDTO createPostDTO = mock(CreatePostDTO.class);
    User currentUser = mock(User.class);

    when(createPostDTO.getTitle()).thenReturn("New Post");
    when(createPostDTO.getContent()).thenReturn("Post Content");
    when(createPostDTO.getIsPublic()).thenReturn(JsonNullable.of(true));

    Post savedPost = Post.builder()
      .user(currentUser)
      .title("New Post")
      .content("Post Content")
      .isPublic(true)
      .build();

    when(postsRepository.save(any(Post.class))).thenReturn(savedPost);

    PostResponseDTO responseDTO = PostResponseDTO.builder().title("New Post").build();
    when(postMapper.toPostResponseDTO(savedPost)).thenReturn(responseDTO);

    PostResponseDTO result = postsService.createPost(createPostDTO, currentUser);

    assertEquals(responseDTO, result);
    verify(postsRepository).save(any(Post.class));
    verify(postMapper).toPostResponseDTO(savedPost);
  }

  @Test
  void createPost_WhenIsPublicIsNotPresent_ShouldSaveAndReturnMappedPostWithIsPublicIsFalse() {
    CreatePostDTO createPostDTO = mock(CreatePostDTO.class);
    User currentUser = mock(User.class);

    when(createPostDTO.getTitle()).thenReturn("Private Post");
    when(createPostDTO.getContent()).thenReturn("Private Content");
    when(createPostDTO.getIsPublic()).thenReturn(JsonNullable.undefined());

    Post savedPost = Post.builder()
      .user(currentUser)
      .title("Private Post")
      .content("Private Content")
      .isPublic(false)
      .build();

    when(postsRepository.save(any(Post.class))).thenReturn(savedPost);

    PostResponseDTO responseDTO = PostResponseDTO.builder().title("Private Post").build();
    when(postMapper.toPostResponseDTO(savedPost)).thenReturn(responseDTO);

    PostResponseDTO result = postsService.createPost(createPostDTO, currentUser);

    assertEquals(responseDTO, result);
    verify(postsRepository).save(any(Post.class));
    verify(postMapper).toPostResponseDTO(savedPost);
  }

  @Test
  void updatePost_WhenPostExists_ShouldAssignAndSaveAndReturnMappedPost() {
    String postId = "post-id";
    User currentUser = mock(User.class);
    when(currentUser.getId()).thenReturn("user-id");
    UpdatePostDTO updatePostDTO = mock(UpdatePostDTO.class);

    Post existingPost = Post.builder().id(postId).user(currentUser).title("Old Title").build();
    Post updatedPost = Post.builder().id(postId).user(currentUser).title("Updated Title").build();
    PostResponseDTO responseDTO = PostResponseDTO.builder().title("Updated Title").build();

    when(postsRepository.findOne(any(Specification.class))).thenReturn(Optional.of(existingPost));
    // Simulate ObjectUtils.assign by manually updating the post
    // (since ObjectUtils.assign is not mocked, we assume it works as expected)
    when(postsRepository.save(existingPost)).thenReturn(updatedPost);
    when(postMapper.toPostResponseDTO(updatedPost)).thenReturn(responseDTO);

    PostResponseDTO result = postsService.updatePost(postId, updatePostDTO, currentUser);

    verify(postsRepository).findOne(any(Specification.class));
    verify(postsRepository).save(existingPost);
    verify(postMapper).toPostResponseDTO(updatedPost);
    assertEquals(responseDTO, result);
  }

  @Test
  void updatePost_WhenPostDoesNotExist_ShouldThrowException() {
    String postId = "missing-id";
    User currentUser = mock(User.class);
    UpdatePostDTO updatePostDTO = mock(UpdatePostDTO.class);

    when(currentUser.getId()).thenReturn("user-id");
    when(postsRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

    assertThrows(PostNotFoundException.class, () ->
      postsService.updatePost(postId, updatePostDTO, currentUser)
    );
    verify(postsRepository).findOne(any(Specification.class));
    verifyNoInteractions(postMapper);
    verify(postsRepository, times(0)).save(any(Post.class));
  }

  @Test
  void deletePost_WhenPostExists_ShouldSetDeletedTimestampAndSave() {
    String postId = "post-id";
    User currentUser = mock(User.class);
    when(currentUser.getId()).thenReturn("user-id");

    Post existingPost = Post.builder().id(postId).user(currentUser).build();

    when(postsRepository.findOne(any(Specification.class))).thenReturn(Optional.of(existingPost));
    when(postsRepository.save(existingPost)).thenReturn(existingPost);

    postsService.deletePost(postId, currentUser);

    verify(postsRepository).findOne(any(Specification.class));
    verify(postsRepository).save(existingPost);
    assertTrue(existingPost.getDeletedTimestamp() != null);
  }

  @Test
  void deletePost_WhenPostDoesNotExist_ShouldThrowException() {
    String postId = "missing-id";
    User currentUser = mock(User.class);
    when(currentUser.getId()).thenReturn("user-id");

    when(postsRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

    assertThrows(PostNotFoundException.class, () -> postsService.deletePost(postId, currentUser));
    verify(postsRepository).findOne(any(Specification.class));
    verify(postsRepository, times(0)).save(any(Post.class));
  }

  @Test
  void restorePost_WhenDeletedPostExists_ShouldRestoreAndReturnMappedPost() {
    String postId = "deleted-id";
    User currentUser = mock(User.class);
    when(currentUser.getId()).thenReturn("user-id");

    Post deletedPost = Post.builder()
      .id(postId)
      .user(currentUser)
      .deletedTimestamp(java.time.Instant.now())
      .build();

    Post restoredPost = Post.builder().id(postId).user(currentUser).deletedTimestamp(null).build();

    PostResponseDTO responseDTO = PostResponseDTO.builder().title("Restored Post").build();

    when(postsRepository.findOne(any(Specification.class))).thenReturn(Optional.of(deletedPost));
    when(postsRepository.save(deletedPost)).thenReturn(restoredPost);
    when(postMapper.toPostResponseDTO(restoredPost)).thenReturn(responseDTO);

    PostResponseDTO result = postsService.restorePost(postId, currentUser);

    verify(postsRepository).findOne(any(Specification.class));
    verify(postsRepository).save(deletedPost);
    verify(postMapper).toPostResponseDTO(restoredPost);
    assertEquals(responseDTO, result);
    assertEquals(null, deletedPost.getDeletedTimestamp());
  }

  @Test
  void restorePost_WhenDeletedPostDoesNotExist_ShouldThrowException() {
    String postId = "missing-id";
    User currentUser = mock(User.class);
    when(currentUser.getId()).thenReturn("user-id");

    when(postsRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

    assertThrows(PostNotFoundException.class, () -> postsService.restorePost(postId, currentUser));
    verify(postsRepository).findOne(any(Specification.class));
    verify(postsRepository, times(0)).save(any(Post.class));
    verifyNoInteractions(postMapper);
  }
}
