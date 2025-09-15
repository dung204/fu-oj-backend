package com.example.modules.posts.controllers;

import static com.example.base.utils.AppRoutes.POSTS_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.base.BaseControllerIntegrationTest;
import com.example.base.dtos.ErrorResponseDTO;
import com.example.base.dtos.PaginatedSuccessResponseDTO;
import com.example.base.dtos.PaginatedSuccessResponseDTO.Metadata;
import com.example.base.dtos.PaginatedSuccessResponseDTO.Order;
import com.example.base.dtos.PaginatedSuccessResponseDTO.Pagination;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.services.JwtService;
import com.example.modules.posts.dtos.CreatePostDTO;
import com.example.modules.posts.dtos.PostResponseDTO;
import com.example.modules.posts.dtos.UpdatePostDTO;
import com.example.modules.posts.entities.Post;
import com.example.modules.posts.repositories.PostsRepository;
import com.example.modules.users.entities.User;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.web.util.UriComponentsBuilder;

@Sql(
  statements = {
    "INSERT INTO accounts (id, created_timestamp, updated_timestamp, email, password, role) VALUES ('d449ffc6-7573-4781-8c72-020ab5f435ea', NOW(), NOW(), 'email@example.com', '$2a$10$qLGDd6oa1eZxcBvA3sYIROBeN2nmcvXBONafYzKiLwTKaAWLqL.PG', 'USER')",
    "INSERT INTO users (id, account_id, created_timestamp, updated_timestamp) VALUES ('6488a2d2-daed-443e-94f1-d86529c1d46f' ,'d449ffc6-7573-4781-8c72-020ab5f435ea', NOW(), NOW())",
    "INSERT INTO posts (id, title, content, is_public, user_id, created_timestamp, updated_timestamp) VALUES ('67bf275a-01df-43b5-b87a-193d1a1c0983', 'Spring Boot Guide', 'Content A', TRUE, '6488a2d2-daed-443e-94f1-d86529c1d46f', NOW(), NOW())",
    "INSERT INTO posts (id, title, content, is_public, user_id, created_timestamp, updated_timestamp) VALUES ('f481a630-3fa7-4f96-92b9-018790ae8d6b', 'Private Post', 'Content B', FALSE, '6488a2d2-daed-443e-94f1-d86529c1d46f', NOW(), NOW())",
    "INSERT INTO posts (id, title, content, is_public, user_id, created_timestamp, updated_timestamp, deleted_timestamp) VALUES ('aa84100d-df3f-473e-acbc-b8cb179bea24', 'Deleted Public', 'Content B', TRUE, '6488a2d2-daed-443e-94f1-d86529c1d46f', NOW(), NOW(), NOW())",
  },
  executionPhase = ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
  statements = { "DELETE FROM posts", "DELETE FROM users", "DELETE FROM accounts" },
  executionPhase = ExecutionPhase.AFTER_TEST_METHOD
)
public class PostsControllerIntegrationTest extends BaseControllerIntegrationTest {

  @Autowired
  private JwtService jwtService;

  @Autowired
  private PostsRepository postsRepository;

  @Test
  void getAllPosts_ShouldReturnOnlyPublicAndDoNotReturnDeletedOrPrivatePosts() throws Exception {
    ResponseEntity<PaginatedSuccessResponseDTO<PostResponseDTO>> response = restTemplate.exchange(
      POSTS_PREFIX,
      HttpMethod.GET,
      HttpEntity.EMPTY,
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(200, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());

    List<PostResponseDTO> posts = response.getBody().getData();
    assertTrue(posts.size() == 1);
    assertTrue(
      posts.stream().allMatch(post -> post.getIsPublic() && post.getDeletedTimestamp() == null)
    );

    Metadata metadata = response.getBody().getMetadata();
    assertNotNull(metadata);

    Pagination pagination = metadata.getPagination();
    assertNotNull(pagination);
    assertEquals(1, pagination.getCurrentPage());
    assertEquals(10, pagination.getPageSize());
    assertEquals(1, pagination.getTotal());
    assertEquals(1, pagination.getTotalPages());
    assertFalse(pagination.isHasPreviousPage());
    assertFalse(pagination.isHasNextPage());

    Map<String, Object> filters = metadata.getFilters();
    assertNotNull(filters);
    assertTrue(filters.isEmpty());

    List<Order> orders = metadata.getOrder();
    assertNotNull(orders);
    assertTrue(orders.isEmpty());
  }

  @Test
  void getAllPosts_WhenProvidedInRequest_ShouldHandleSearchParameters() throws Exception {
    User user = getUser();

    String url = UriComponentsBuilder.fromPath(POSTS_PREFIX)
      .queryParam("title", "Spring")
      .queryParam("user", user.getId())
      .queryParam("pageSize", 50)
      .queryParam(
        "order",
        List.of(
          "createdTimestamp:desc",
          "updatedTimestamp:asc",
          "deletedTimestamp:desc",
          "title:asc"
        )
      )
      .toUriString();

    ResponseEntity<PaginatedSuccessResponseDTO<PostResponseDTO>> response = restTemplate.exchange(
      url,
      HttpMethod.GET,
      HttpEntity.EMPTY,
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(200, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());

    List<PostResponseDTO> posts = response.getBody().getData();
    assertTrue(posts.size() == 1);
    assertTrue(
      posts.stream().allMatch(post -> post.getIsPublic() && post.getDeletedTimestamp() == null)
    );

    Metadata metadata = response.getBody().getMetadata();
    assertNotNull(metadata);

    Pagination pagination = metadata.getPagination();
    assertNotNull(pagination);
    assertEquals(1, pagination.getCurrentPage());
    assertEquals(50, pagination.getPageSize());
    assertEquals(1, pagination.getTotal());
    assertEquals(1, pagination.getTotalPages());
    assertFalse(pagination.isHasPreviousPage());
    assertFalse(pagination.isHasNextPage());

    Map<String, Object> filters = metadata.getFilters();
    assertNotNull(filters);
    assertTrue(filters.size() == 2);
    assertEquals("Spring", filters.get("title"));
    assertEquals(user.getId(), filters.get("user"));

    List<Order> orders = metadata.getOrder();
    assertNotNull(orders);
    assertTrue(orders.size() == 4);
    assertEquals("createdTimestamp", orders.get(0).getField());
    assertEquals("desc", orders.get(0).getDirection());
    assertEquals("updatedTimestamp", orders.get(1).getField());
    assertEquals("asc", orders.get(1).getDirection());
    assertEquals("deletedTimestamp", orders.get(2).getField());
    assertEquals("desc", orders.get(2).getDirection());
    assertEquals("title", orders.get(3).getField());
    assertEquals("asc", orders.get(3).getDirection());
  }

  @Test
  void getAllPosts_WhenPageIsSmallerThan1_ShouldReturnBadRequest() throws Exception {
    String url = UriComponentsBuilder.fromPath(POSTS_PREFIX).queryParam("page", 0).toUriString();

    ResponseEntity<PaginatedSuccessResponseDTO<PostResponseDTO>> response = restTemplate.exchange(
      url,
      HttpMethod.GET,
      HttpEntity.EMPTY,
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void getAllPosts_WhenPageIsNotANumber_ShouldReturnBadRequest() throws Exception {
    String url = UriComponentsBuilder.fromPath(POSTS_PREFIX)
      .queryParam("page", "abc")
      .toUriString();

    ResponseEntity<PaginatedSuccessResponseDTO<PostResponseDTO>> response = restTemplate.exchange(
      url,
      HttpMethod.GET,
      HttpEntity.EMPTY,
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void getAllPosts_WhenPageSizeIsSmallerThan1_ShouldReturnBadRequest() throws Exception {
    String url = UriComponentsBuilder.fromPath(POSTS_PREFIX)
      .queryParam("pageSize", 0)
      .toUriString();

    ResponseEntity<PaginatedSuccessResponseDTO<PostResponseDTO>> response = restTemplate.exchange(
      url,
      HttpMethod.GET,
      HttpEntity.EMPTY,
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void getAllPosts_WhenPageSizeIsNotANumber_ShouldReturnBadRequest() throws Exception {
    String url = UriComponentsBuilder.fromPath(POSTS_PREFIX)
      .queryParam("pageSize", "abc")
      .toUriString();

    ResponseEntity<PaginatedSuccessResponseDTO<PostResponseDTO>> response = restTemplate.exchange(
      url,
      HttpMethod.GET,
      HttpEntity.EMPTY,
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void getAllPosts_WhenOrderIsInvalid_ShouldReturnBadRequest() throws Exception {
    String url = UriComponentsBuilder.fromPath(POSTS_PREFIX)
      .queryParam("order", "invalidField:asc")
      .toUriString();

    ResponseEntity<PaginatedSuccessResponseDTO<PostResponseDTO>> response = restTemplate.exchange(
      url,
      HttpMethod.GET,
      HttpEntity.EMPTY,
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void getPostById_WhenPostNotExist_ShouldReturnNotFound() throws Exception {
    String postId = "not-exist";

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      POSTS_PREFIX + "/" + postId,
      HttpMethod.GET,
      HttpEntity.EMPTY,
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(404, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void getPostById_WhenUserIsNotLoggedInAndPublicPostExists_ShouldReturnPost() throws Exception {
    String postId = "67bf275a-01df-43b5-b87a-193d1a1c0983";

    Post post = postsRepository.findById(postId).get();
    User user = getUser();

    ResponseEntity<SuccessResponseDTO<PostResponseDTO>> response = restTemplate.exchange(
      POSTS_PREFIX + "/" + postId,
      HttpMethod.GET,
      HttpEntity.EMPTY,
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(200, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());

    PostResponseDTO foundPost = response.getBody().getData();
    assertNotNull(foundPost);
    assertEquals(post.getId(), foundPost.getId());
    assertEquals(post.getTitle(), foundPost.getTitle());
    assertEquals(post.getContent(), foundPost.getContent());
    assertEquals(post.getIsPublic(), foundPost.getIsPublic());
    assertEquals(post.getCreatedTimestamp().toString(), foundPost.getCreatedTimestamp());
    assertEquals(post.getUpdatedTimestamp().toString(), foundPost.getUpdatedTimestamp());
    assertEquals(post.getDeletedTimestamp(), foundPost.getDeletedTimestamp());
    assertEquals(user.getId(), foundPost.getUser().getId());
    assertEquals(user.getAccount().getEmail(), foundPost.getUser().getEmail());
  }

  @Test
  void getPostById_WhenUserIsNotLoggedInAndHisPrivatePostExists_ShouldReturnNotFound()
    throws Exception {
    String postId = "f481a630-3fa7-4f96-92b9-018790ae8d6b";

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      POSTS_PREFIX + "/" + postId,
      HttpMethod.GET,
      HttpEntity.EMPTY,
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(404, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void getPostById_WhenUserIsNotLoggedInAndHisDeletedPostExists_ShouldReturnNotFound()
    throws Exception {
    String postId = "aa84100d-df3f-473e-acbc-b8cb179bea24";

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      POSTS_PREFIX + "/" + postId,
      HttpMethod.GET,
      HttpEntity.EMPTY,
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(404, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void getPostById_WhenUserIsLoggedInAndPublicPostExists_ShouldReturnPost() throws Exception {
    String postId = "67bf275a-01df-43b5-b87a-193d1a1c0983";
    Post post = postsRepository.findById(postId).get();

    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    ResponseEntity<SuccessResponseDTO<PostResponseDTO>> response = restTemplate.exchange(
      POSTS_PREFIX + "/" + postId,
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(200, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());

    PostResponseDTO foundPost = response.getBody().getData();
    assertNotNull(foundPost);
    assertEquals(post.getId(), foundPost.getId());
    assertEquals(post.getTitle(), foundPost.getTitle());
    assertEquals(post.getContent(), foundPost.getContent());
    assertEquals(post.getIsPublic(), foundPost.getIsPublic());
    assertEquals(post.getCreatedTimestamp().toString(), foundPost.getCreatedTimestamp());
    assertEquals(post.getUpdatedTimestamp().toString(), foundPost.getUpdatedTimestamp());
    assertEquals(post.getDeletedTimestamp(), foundPost.getDeletedTimestamp());
    assertEquals(user.getId(), foundPost.getUser().getId());
    assertEquals(user.getAccount().getEmail(), foundPost.getUser().getEmail());
  }

  @Test
  void getPostById_WhenUserIsLoggedInAndHisPrivatePostExists_ShouldReturnPost() throws Exception {
    String postId = "f481a630-3fa7-4f96-92b9-018790ae8d6b";
    Post post = postsRepository.findById(postId).get();

    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    ResponseEntity<SuccessResponseDTO<PostResponseDTO>> response = restTemplate.exchange(
      POSTS_PREFIX + "/" + postId,
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(200, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());

    PostResponseDTO foundPost = response.getBody().getData();
    assertNotNull(foundPost);
    assertEquals(post.getId(), foundPost.getId());
    assertEquals(post.getTitle(), foundPost.getTitle());
    assertEquals(post.getContent(), foundPost.getContent());
    assertEquals(post.getIsPublic(), foundPost.getIsPublic());
    assertEquals(post.getCreatedTimestamp().toString(), foundPost.getCreatedTimestamp());
    assertEquals(post.getUpdatedTimestamp().toString(), foundPost.getUpdatedTimestamp());
    assertEquals(post.getDeletedTimestamp(), foundPost.getDeletedTimestamp());
    assertEquals(user.getId(), foundPost.getUser().getId());
    assertEquals(user.getAccount().getEmail(), foundPost.getUser().getEmail());
  }

  @Test
  void getPostById_WhenUserIsLoggedInAndHisDeletedPostExists_ShouldReturnPost() throws Exception {
    String postId = "aa84100d-df3f-473e-acbc-b8cb179bea24";
    Post post = postsRepository.findById(postId).get();

    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    ResponseEntity<SuccessResponseDTO<PostResponseDTO>> response = restTemplate.exchange(
      POSTS_PREFIX + "/" + postId,
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(200, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());

    PostResponseDTO foundPost = response.getBody().getData();
    assertNotNull(foundPost);
    assertEquals(post.getId(), foundPost.getId());
    assertEquals(post.getTitle(), foundPost.getTitle());
    assertEquals(post.getContent(), foundPost.getContent());
    assertEquals(post.getIsPublic(), foundPost.getIsPublic());
    assertEquals(post.getCreatedTimestamp().toString(), foundPost.getCreatedTimestamp());
    assertEquals(post.getUpdatedTimestamp().toString(), foundPost.getUpdatedTimestamp());
    assertEquals(post.getDeletedTimestamp().toString(), foundPost.getDeletedTimestamp());
    assertEquals(user.getId(), foundPost.getUser().getId());
    assertEquals(user.getAccount().getEmail(), foundPost.getUser().getEmail());
  }

  @Test
  void createPost_WhenUserIsNotLoggedIn_ShouldReturnUnauthorized() throws Exception {
    CreatePostDTO createPostDTO = CreatePostDTO.builder()
      .title("New Post")
      .content("Post content")
      .build();

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      POSTS_PREFIX,
      HttpMethod.POST,
      new HttpEntity<>(createPostDTO),
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void createPost_WhenValidRequest_ShouldReturnCreatedPost() throws Exception {
    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    CreatePostDTO createPostDTO = CreatePostDTO.builder()
      .title("New Post")
      .content("Post content")
      .build();

    ResponseEntity<SuccessResponseDTO<PostResponseDTO>> response = restTemplate.exchange(
      POSTS_PREFIX,
      HttpMethod.POST,
      new HttpEntity<>(createPostDTO, headers),
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(201, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());

    PostResponseDTO createdPost = response.getBody().getData();
    assertNotNull(createdPost);
    assertEquals(createPostDTO.getTitle(), createdPost.getTitle());
    assertEquals(createPostDTO.getContent(), createdPost.getContent());
    assertFalse(createdPost.getIsPublic());
    assertNull(createdPost.getDeletedTimestamp());
    assertEquals(user.getId(), createdPost.getUser().getId());
    assertEquals(user.getAccount().getEmail(), createdPost.getUser().getEmail());
  }

  @Test
  void createPost_WhenTitleIsMissing_ShouldReturnBadRequest() throws Exception {
    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    CreatePostDTO invalidDto = CreatePostDTO.builder().content("content").build();

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      POSTS_PREFIX,
      HttpMethod.POST,
      new HttpEntity<>(invalidDto, headers),
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void createPost_WhenTitleIsBlank_ShouldReturnBadRequest() throws Exception {
    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    CreatePostDTO invalidDto = CreatePostDTO.builder()
      .title("             ")
      .content("content")
      .build();

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      POSTS_PREFIX,
      HttpMethod.POST,
      new HttpEntity<>(invalidDto, headers),
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void createPost_WhenTitleExceeds255Characters_ShouldReturnBadRequest() throws Exception {
    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    CreatePostDTO invalidDto = CreatePostDTO.builder()
      .title("a".repeat(256))
      .content("content")
      .build();

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      POSTS_PREFIX,
      HttpMethod.POST,
      new HttpEntity<>(invalidDto, headers),
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void createPost_WhenContentIsMissing_ShouldReturnBadRequest() throws Exception {
    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    CreatePostDTO invalidDto = CreatePostDTO.builder().title("title").build();

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      POSTS_PREFIX,
      HttpMethod.POST,
      new HttpEntity<>(invalidDto, headers),
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void createPost_WhenContentIsBlank_ShouldReturnBadRequest() throws Exception {
    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    CreatePostDTO invalidDto = CreatePostDTO.builder()
      .title("title")
      .content("             ")
      .build();

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      POSTS_PREFIX,
      HttpMethod.POST,
      new HttpEntity<>(invalidDto, headers),
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void updatePost_WhenUserIsNotLoggedIn_ShouldReturnUnauthorized() throws Exception {
    String postId = "67bf275a-01df-43b5-b87a-193d1a1c0983";

    UpdatePostDTO updatePostDTO = UpdatePostDTO.builder()
      .title(JsonNullable.of("Title"))
      .content(JsonNullable.of("Content"))
      .build();

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      POSTS_PREFIX + "/" + postId,
      HttpMethod.PATCH,
      new HttpEntity<>(updatePostDTO),
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void updatePost_WhenPostDoesNotExist_ShouldReturnNotFound() throws Exception {
    String postId = "not-found";

    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    UpdatePostDTO updatePostDTO = UpdatePostDTO.builder()
      .title(JsonNullable.of("Title"))
      .content(JsonNullable.of("Content"))
      .build();

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      POSTS_PREFIX + "/" + postId,
      HttpMethod.PATCH,
      new HttpEntity<>(updatePostDTO, headers),
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(404, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void updatePost_WhenValidRequest_ShouldUpdateAndReturnUpdatedPost() throws Exception {
    String postId = "67bf275a-01df-43b5-b87a-193d1a1c0983";
    Post post = postsRepository.findById(postId).get();

    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    UpdatePostDTO updatePostDTO = UpdatePostDTO.builder()
      .title(JsonNullable.of("Updated Title"))
      .content(JsonNullable.of("Updated Content"))
      .build();

    ResponseEntity<SuccessResponseDTO<PostResponseDTO>> response = restTemplate.exchange(
      POSTS_PREFIX + "/" + postId,
      HttpMethod.PATCH,
      new HttpEntity<>(updatePostDTO, headers),
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(200, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());

    PostResponseDTO updatedPost = response.getBody().getData();
    assertNotNull(updatedPost);
    assertEquals(post.getId(), updatedPost.getId());
    assertEquals(updatePostDTO.getTitle().get(), updatedPost.getTitle());
    assertEquals(updatePostDTO.getContent().get(), updatedPost.getContent());
    assertEquals(post.getIsPublic(), updatedPost.getIsPublic());
    assertEquals(post.getCreatedTimestamp().toString(), updatedPost.getCreatedTimestamp());
    assertNotEquals(post.getUpdatedTimestamp().toString(), updatedPost.getUpdatedTimestamp());
    assertEquals(user.getId(), updatedPost.getUser().getId());
    assertEquals(user.getAccount().getEmail(), updatedPost.getUser().getEmail());
  }

  @Test
  void updatePost_WhenBodyIsEmpty_ShouldNotUpdateAndReturnOriginalPost() throws Exception {
    String postId = "67bf275a-01df-43b5-b87a-193d1a1c0983";
    Post post = postsRepository.findById(postId).get();

    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    UpdatePostDTO updatePostDTO = UpdatePostDTO.builder().build();

    ResponseEntity<SuccessResponseDTO<PostResponseDTO>> response = restTemplate.exchange(
      POSTS_PREFIX + "/" + postId,
      HttpMethod.PATCH,
      new HttpEntity<>(updatePostDTO, headers),
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(200, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());

    PostResponseDTO updatedPost = response.getBody().getData();
    assertNotNull(updatedPost);
    assertEquals(post.getId(), updatedPost.getId());
    assertEquals(post.getTitle(), updatedPost.getTitle());
    assertEquals(post.getContent(), updatedPost.getContent());
    assertEquals(post.getIsPublic(), updatedPost.getIsPublic());
    assertEquals(post.getCreatedTimestamp().toString(), updatedPost.getCreatedTimestamp());
    assertEquals(post.getUpdatedTimestamp().toString(), updatedPost.getUpdatedTimestamp());
    assertEquals(user.getId(), updatedPost.getUser().getId());
    assertEquals(user.getAccount().getEmail(), updatedPost.getUser().getEmail());
  }

  @Test
  void updatePost_WhenUpdateWithOnlyTitle_ShouldUpdateAndReturnUpdatedPost() throws Exception {
    String postId = "67bf275a-01df-43b5-b87a-193d1a1c0983";
    Post post = postsRepository.findById(postId).get();

    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    UpdatePostDTO updatePostDTO = UpdatePostDTO.builder()
      .title(JsonNullable.of("Updated Title"))
      .build();

    ResponseEntity<SuccessResponseDTO<PostResponseDTO>> response = restTemplate.exchange(
      POSTS_PREFIX + "/" + postId,
      HttpMethod.PATCH,
      new HttpEntity<>(updatePostDTO, headers),
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(200, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());

    PostResponseDTO updatedPost = response.getBody().getData();
    assertNotNull(updatedPost);
    assertEquals(post.getId(), updatedPost.getId());
    assertEquals(updatePostDTO.getTitle().get(), updatedPost.getTitle());
    assertEquals(post.getContent(), updatedPost.getContent());
    assertEquals(post.getIsPublic(), updatedPost.getIsPublic());
    assertEquals(post.getCreatedTimestamp().toString(), updatedPost.getCreatedTimestamp());
    assertNotEquals(post.getUpdatedTimestamp().toString(), updatedPost.getUpdatedTimestamp());
    assertEquals(user.getId(), updatedPost.getUser().getId());
    assertEquals(user.getAccount().getEmail(), updatedPost.getUser().getEmail());
  }

  @Test
  void updatePost_WhenUpdateWithOnlyContent_ShouldUpdateAndReturnUpdatedPost() throws Exception {
    String postId = "67bf275a-01df-43b5-b87a-193d1a1c0983";
    Post post = postsRepository.findById(postId).get();

    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    UpdatePostDTO updatePostDTO = UpdatePostDTO.builder()
      .content(JsonNullable.of("Updated Content"))
      .build();

    ResponseEntity<SuccessResponseDTO<PostResponseDTO>> response = restTemplate.exchange(
      POSTS_PREFIX + "/" + postId,
      HttpMethod.PATCH,
      new HttpEntity<>(updatePostDTO, headers),
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(200, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());

    PostResponseDTO updatedPost = response.getBody().getData();
    assertNotNull(updatedPost);
    assertEquals(post.getId(), updatedPost.getId());
    assertEquals(post.getTitle(), updatedPost.getTitle());
    assertEquals(updatePostDTO.getContent().get(), updatedPost.getContent());
    assertEquals(post.getIsPublic(), updatedPost.getIsPublic());
    assertEquals(post.getCreatedTimestamp().toString(), updatedPost.getCreatedTimestamp());
    assertNotEquals(post.getUpdatedTimestamp().toString(), updatedPost.getUpdatedTimestamp());
    assertEquals(user.getId(), updatedPost.getUser().getId());
    assertEquals(user.getAccount().getEmail(), updatedPost.getUser().getEmail());
  }

  @Test
  void updatePost_WhenUpdateWithOnlyIsPublic_ShouldUpdateAndReturnUpdatedPost() throws Exception {
    String postId = "67bf275a-01df-43b5-b87a-193d1a1c0983";
    Post post = postsRepository.findById(postId).get();

    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    UpdatePostDTO updatePostDTO = UpdatePostDTO.builder().isPublic(JsonNullable.of(false)).build();

    ResponseEntity<SuccessResponseDTO<PostResponseDTO>> response = restTemplate.exchange(
      POSTS_PREFIX + "/" + postId,
      HttpMethod.PATCH,
      new HttpEntity<>(updatePostDTO, headers),
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(200, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());

    PostResponseDTO updatedPost = response.getBody().getData();
    assertNotNull(updatedPost);
    assertEquals(post.getId(), updatedPost.getId());
    assertEquals(post.getTitle(), updatedPost.getTitle());
    assertEquals(post.getContent(), updatedPost.getContent());
    assertEquals(updatePostDTO.getIsPublic().get(), updatedPost.getIsPublic());
    assertEquals(post.getCreatedTimestamp().toString(), updatedPost.getCreatedTimestamp());
    assertNotEquals(post.getUpdatedTimestamp().toString(), updatedPost.getUpdatedTimestamp());
    assertEquals(user.getId(), updatedPost.getUser().getId());
    assertEquals(user.getAccount().getEmail(), updatedPost.getUser().getEmail());
  }

  @Test
  void updatePost_WhenUpdateTitleToNull_ShouldReturnBadRequest() throws Exception {
    String postId = "67bf275a-01df-43b5-b87a-193d1a1c0983";

    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    UpdatePostDTO updatePostDTO = UpdatePostDTO.builder().title(null).build();

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      POSTS_PREFIX + "/" + postId,
      HttpMethod.PATCH,
      new HttpEntity<>(updatePostDTO, headers),
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void updatePost_WhenTitleExceeds255Characters_ShouldReturnBadRequest() throws Exception {
    String postId = "67bf275a-01df-43b5-b87a-193d1a1c0983";

    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    UpdatePostDTO updatePostDTO = UpdatePostDTO.builder()
      .title(JsonNullable.of("a".repeat(256)))
      .build();

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      POSTS_PREFIX + "/" + postId,
      HttpMethod.PATCH,
      new HttpEntity<>(updatePostDTO, headers),
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void updatePost_WhenUpdateContentToNull_ShouldReturnBadRequest() throws Exception {
    String postId = "67bf275a-01df-43b5-b87a-193d1a1c0983";

    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    UpdatePostDTO updatePostDTO = UpdatePostDTO.builder().content(null).build();

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      POSTS_PREFIX + "/" + postId,
      HttpMethod.PATCH,
      new HttpEntity<>(updatePostDTO, headers),
      new ParameterizedTypeReference<>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }
}
