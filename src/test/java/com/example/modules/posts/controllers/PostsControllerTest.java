package com.example.modules.posts.controllers;

import static com.example.base.utils.AppRoutes.POSTS_PREFIX;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.base.BaseControllerTest;
import com.example.modules.posts.dtos.CreatePostDTO;
import com.example.modules.posts.dtos.PostResponseDTO;
import com.example.modules.posts.dtos.PostsSearchDTO;
import com.example.modules.posts.dtos.UpdatePostDTO;
import com.example.modules.posts.exceptions.PostNotFoundException;
import com.example.modules.posts.services.PostsService;
import com.example.modules.users.dtos.UserProfileDTO;
import com.example.modules.users.entities.User;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@WebMvcTest(PostsController.class)
public class PostsControllerTest extends BaseControllerTest {

  @MockitoBean
  private PostsService postsService;

  @Test
  void getAllPosts_WhenValidRequest_ShouldReturnPaginatedPosts() throws Exception {
    // Given
    Page<PostResponseDTO> mockPage = new PageImpl<>(
      List.of(PostResponseDTO.builder().id("1").build(), PostResponseDTO.builder().id("2").build()),
      PageRequest.of(0, 10),
      2
    );

    when(postsService.findAllPublicPosts(any(PostsSearchDTO.class))).thenReturn(mockPage);

    // When & Then
    mockMvc
      .perform(get(POSTS_PREFIX).contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(200))
      .andExpect(jsonPath("$.message").isString())
      .andExpect(jsonPath("$.data").isArray())
      .andExpect(jsonPath("$.data[0].id").value("1"))
      .andExpect(jsonPath("$.data[1].id").value("2"))
      .andExpect(jsonPath("$.metadata.pagination.currentPage").isNumber())
      .andExpect(jsonPath("$.metadata.pagination.pageSize").isNumber())
      .andExpect(jsonPath("$.metadata.pagination.total").isNumber())
      .andExpect(jsonPath("$.metadata.pagination.totalPages").isNumber())
      .andExpect(jsonPath("$.metadata.pagination.hasNextPage").isBoolean())
      .andExpect(jsonPath("$.metadata.pagination.hasPreviousPage").isBoolean())
      .andExpect(jsonPath("$.metadata.order").isArray())
      .andExpect(jsonPath("$.metadata.order").isEmpty());
  }

  @Test
  void getAllPosts_WhenNoPostsExist_ShouldReturnEmptyPage() throws Exception {
    Page<PostResponseDTO> emptyPage = new PageImpl<>(
      Collections.emptyList(),
      PageRequest.of(0, 10),
      0
    );
    when(postsService.findAllPublicPosts(any(PostsSearchDTO.class))).thenReturn(emptyPage);

    mockMvc
      .perform(get(POSTS_PREFIX).contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(200))
      .andExpect(jsonPath("$.message").isString())
      .andExpect(jsonPath("$.data").isArray())
      .andExpect(jsonPath("$.data").isEmpty())
      .andExpect(jsonPath("$.metadata.pagination.currentPage").value(1))
      .andExpect(jsonPath("$.metadata.pagination.pageSize").value(10))
      .andExpect(jsonPath("$.metadata.pagination.total").value(0))
      .andExpect(jsonPath("$.metadata.pagination.totalPages").value(0))
      .andExpect(jsonPath("$.metadata.pagination.hasNextPage").value(false))
      .andExpect(jsonPath("$.metadata.pagination.hasPreviousPage").value(false))
      .andExpect(jsonPath("$.metadata.order").isArray())
      .andExpect(jsonPath("$.metadata.order").isEmpty())
      .andExpect(jsonPath("$.metadata.filters").isMap())
      .andExpect(jsonPath("$.metadata.filters").isEmpty());
  }

  @Test
  void getAllPosts_WhenProvidedInRequest_ShouldHandleSearchParameters() throws Exception {
    // Given
    PostsSearchDTO postsSearchDTO = new PostsSearchDTO();
    postsSearchDTO.setPageSize(50);
    postsSearchDTO.setOrder(
      List.of("createdTimestamp:desc", "updatedTimestamp:asc", "deletedTimestamp:desc", "title:asc")
    );
    postsSearchDTO.setTitle("post title");
    postsSearchDTO.setUser("user id");

    Page<PostResponseDTO> mockPage = new PageImpl<>(
      List.of(PostResponseDTO.builder().id("1").title("Filtered Post").build()),
      PageRequest.of(
        postsSearchDTO.getPage() - 1,
        postsSearchDTO.getPageSize(),
        Sort.by(
          Order.desc("createdTimestamp"),
          Order.asc("updatedTimestamp"),
          Order.desc("deletedTimestamp"),
          Order.asc("title")
        )
      ),
      1
    );

    when(postsService.findAllPublicPosts(any(PostsSearchDTO.class))).thenReturn(mockPage);

    // When & Then
    mockMvc
      .perform(
        get(POSTS_PREFIX)
          .queryParams(toQueryParams(postsSearchDTO))
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(200))
      .andExpect(jsonPath("$.message").isString())
      .andExpect(jsonPath("$.data").isArray())
      .andExpect(jsonPath("$.data[0].id").value("1"))
      .andExpect(
        jsonPath("$.metadata.pagination.currentPage").value(
          mockPage.getPageable().getPageNumber() + 1
        )
      )
      .andExpect(
        jsonPath("$.metadata.pagination.pageSize").value(mockPage.getPageable().getPageSize())
      )
      .andExpect(jsonPath("$.metadata.pagination.total").value(mockPage.getTotalElements()))
      .andExpect(jsonPath("$.metadata.pagination.totalPages").value(mockPage.getTotalPages()))
      .andExpect(jsonPath("$.metadata.pagination.hasNextPage").value(false))
      .andExpect(jsonPath("$.metadata.pagination.hasPreviousPage").value(false))
      .andExpect(jsonPath("$.metadata.order").isArray())
      .andExpect(jsonPath("$.metadata.order[0].field").value("createdTimestamp"))
      .andExpect(jsonPath("$.metadata.order[0].direction").value("desc"))
      .andExpect(jsonPath("$.metadata.order[1].field").value("updatedTimestamp"))
      .andExpect(jsonPath("$.metadata.order[1].direction").value("asc"))
      .andExpect(jsonPath("$.metadata.order[2].field").value("deletedTimestamp"))
      .andExpect(jsonPath("$.metadata.order[2].direction").value("desc"))
      .andExpect(jsonPath("$.metadata.order[3].field").value("title"))
      .andExpect(jsonPath("$.metadata.order[3].direction").value("asc"))
      .andExpect(jsonPath("$.metadata.filters").isMap())
      .andExpect(jsonPath("$.metadata.filters.title").value(postsSearchDTO.getTitle()))
      .andExpect(jsonPath("$.metadata.filters.user").value(postsSearchDTO.getUser()));
  }

  @Test
  void getAllPosts_WhenPageIsSmallerThan1_ShouldReturnBadRequest() throws Exception {
    mockMvc
      .perform(get(POSTS_PREFIX).queryParam("page", "-1").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void getAllPosts_WhenPageIsNotANumber_ShouldReturnBadRequest() throws Exception {
    mockMvc
      .perform(get(POSTS_PREFIX).queryParam("page", "aaa").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void getAllPosts_WhenPageSizeIsSmallerThan1_ShouldReturnBadRequest() throws Exception {
    mockMvc
      .perform(
        get(POSTS_PREFIX).queryParam("pageSize", "-1").contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void getAllPosts_WhenPageSizeIsNotANumber_ShouldReturnBadRequest() throws Exception {
    mockMvc
      .perform(
        get(POSTS_PREFIX).queryParam("pageSize", "aaa").contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void getAllPosts_WhenOrderIsInvalid_ShouldReturnBadRequest() throws Exception {
    mockMvc
      .perform(
        get(POSTS_PREFIX)
          .queryParam("order", "invalidField:desc")
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void getPostById_WhenPostExists_ShouldReturnPost() throws Exception {
    // Given
    String postId = "123";
    PostResponseDTO postResponseDTO = PostResponseDTO.builder()
      .id(postId)
      .title("Test Post")
      .build();
    when(postsService.findPostById(any(String.class), any())).thenReturn(postResponseDTO);

    // When & Then
    mockMvc
      .perform(get(POSTS_PREFIX + "/" + postId).contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(200))
      .andExpect(jsonPath("$.message").isString())
      .andExpect(jsonPath("$.data.id").value(postId))
      .andExpect(jsonPath("$.data.title").value("Test Post"));
  }

  @Test
  void getPostById_WhenPostDoesNotExist_ShouldReturnNotFound() throws Exception {
    // Given
    String postId = "not-found";
    when(postsService.findPostById(any(String.class), any())).thenThrow(
      new PostNotFoundException()
    );

    // When & Then
    mockMvc
      .perform(get(POSTS_PREFIX + "/" + postId).contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void createPost_WhenUserIsNotLoggedIn_ShouldReturnUnauthorized() throws Exception {
    mockMvc
      .perform(post(POSTS_PREFIX).contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.status").value(401))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void createPost_WhenValidRequest_ShouldReturnCreatedPost() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();
    User mockUser = payload.user();
    UserProfileDTO mockUserProfile = createMockUserProfile(mockUser);

    CreatePostDTO createPostDTO = CreatePostDTO.builder()
      .title("New Post")
      .content("Post content")
      .build();

    PostResponseDTO postResponseDTO = PostResponseDTO.builder()
      .id("123")
      .title("New Post")
      .content("Post content")
      .user(createMockUserProfile(mockUser))
      .build();

    when(postsService.createPost(any(CreatePostDTO.class), any())).thenReturn(postResponseDTO);

    mockMvc
      .perform(
        post(POSTS_PREFIX)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createPostDTO))
          .header("Authorization", "Bearer " + mockAccessToken)
      )
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.status").value(201))
      .andExpect(jsonPath("$.message").isString())
      .andExpect(jsonPath("$.data.id").value(postResponseDTO.getId()))
      .andExpect(jsonPath("$.data.title").value(postResponseDTO.getTitle()))
      .andExpect(jsonPath("$.data.content").value(postResponseDTO.getContent()))
      .andExpect(jsonPath("$.data.user.id").value(mockUserProfile.getId()))
      .andExpect(jsonPath("$.data.user.email").value(mockUserProfile.getEmail()))
      .andExpect(jsonPath("$.data.user.firstName").value(mockUserProfile.getFirstName()))
      .andExpect(jsonPath("$.data.user.lastName").value(mockUserProfile.getLastName()))
      .andExpect(jsonPath("$.data.user.role").value(mockUserProfile.getRole()));
  }

  @Test
  void createPost_WhenTitleIsMissing_ShouldReturnBadRequest() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();
    CreatePostDTO invalidDto = CreatePostDTO.builder().content("content").build();

    mockMvc
      .perform(
        post(POSTS_PREFIX)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalidDto))
          .header("Authorization", "Bearer " + mockAccessToken)
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void createPost_WhenTitleIsBlank_ShouldReturnBadRequest() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();
    CreatePostDTO invalidDto = CreatePostDTO.builder()
      .title("             ")
      .content("content")
      .build();

    mockMvc
      .perform(
        post(POSTS_PREFIX)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalidDto))
          .header("Authorization", "Bearer " + mockAccessToken)
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void createPost_WhenTitleExceeds255Characters_ShouldReturnBadRequest() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();
    CreatePostDTO invalidDto = CreatePostDTO.builder()
      .title("a".repeat(256))
      .content("content")
      .build();

    mockMvc
      .perform(
        post(POSTS_PREFIX)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalidDto))
          .header("Authorization", "Bearer " + mockAccessToken)
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void createPost_WhenContentIsMissing_ShouldReturnBadRequest() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();
    CreatePostDTO invalidDto = CreatePostDTO.builder().title("title").build();

    mockMvc
      .perform(
        post(POSTS_PREFIX)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalidDto))
          .header("Authorization", "Bearer " + mockAccessToken)
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void createPost_WhenContentIsBlank_ShouldReturnBadRequest() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();
    CreatePostDTO invalidDto = CreatePostDTO.builder()
      .title("title")
      .content("             ")
      .build();

    mockMvc
      .perform(
        post(POSTS_PREFIX)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalidDto))
          .header("Authorization", "Bearer " + mockAccessToken)
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void updatePost_WhenUserIsNotLoggedIn_ShouldReturnUnauthorized() throws Exception {
    String postId = "123";

    mockMvc
      .perform(patch(POSTS_PREFIX + "/" + postId).contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.status").value(401))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void updatePost_WhenPostDoesNotExist_ShouldReturnNotFound() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();
    String postId = "not-found";
    UpdatePostDTO updatePostDTO = UpdatePostDTO.builder()
      .title(JsonNullable.of("Title"))
      .content(JsonNullable.of("Content"))
      .build();

    when(postsService.updatePost(eq(postId), any(UpdatePostDTO.class), any(User.class))).thenThrow(
      new PostNotFoundException()
    );

    mockMvc
      .perform(
        patch(POSTS_PREFIX + "/" + postId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updatePostDTO))
          .header("Authorization", "Bearer " + mockAccessToken)
      )
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void updatePost_WhenValidRequest_ShouldReturnUpdatedPost() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();
    User mockUser = payload.user();
    UserProfileDTO mockUserProfile = createMockUserProfile(mockUser);

    String postId = "123";
    UpdatePostDTO updatePostDTO = UpdatePostDTO.builder()
      .title(JsonNullable.of("Updated Title"))
      .content(JsonNullable.of("Updated Content"))
      .build();

    PostResponseDTO updatedPost = PostResponseDTO.builder()
      .id(postId)
      .title("Updated Title")
      .content("Updated Content")
      .user(mockUserProfile)
      .build();

    when(postsService.updatePost(eq(postId), any(UpdatePostDTO.class), eq(mockUser))).thenReturn(
      updatedPost
    );

    mockMvc
      .perform(
        patch(POSTS_PREFIX + "/" + postId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updatePostDTO))
          .header("Authorization", "Bearer " + mockAccessToken)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(200))
      .andExpect(jsonPath("$.message").isString())
      .andExpect(jsonPath("$.data.id").value(postId))
      .andExpect(jsonPath("$.data.title").value(updatedPost.getTitle()))
      .andExpect(jsonPath("$.data.content").value(updatedPost.getContent()))
      .andExpect(jsonPath("$.data.user.id").value(mockUserProfile.getId()));
  }

  @Test
  void updatePost_WhenBodyIsEmpty_ShouldReturnUpdatedPost() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();
    User mockUser = payload.user();
    UserProfileDTO mockUserProfile = createMockUserProfile(mockUser);

    String postId = "123";

    PostResponseDTO updatedPost = PostResponseDTO.builder()
      .id(postId)
      .title("Title")
      .content("Content")
      .user(mockUserProfile)
      .build();

    when(postsService.updatePost(eq(postId), any(UpdatePostDTO.class), eq(mockUser))).thenReturn(
      updatedPost
    );

    mockMvc
      .perform(
        patch(POSTS_PREFIX + "/" + postId)
          .contentType(MediaType.APPLICATION_JSON)
          .content("{}")
          .header("Authorization", "Bearer " + mockAccessToken)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(200))
      .andExpect(jsonPath("$.message").isString())
      .andExpect(jsonPath("$.data.id").value(postId))
      .andExpect(jsonPath("$.data.title").value(updatedPost.getTitle()))
      .andExpect(jsonPath("$.data.content").value(updatedPost.getContent()))
      .andExpect(jsonPath("$.data.user.id").value(mockUserProfile.getId()));
  }

  @Test
  void updatePost_WhenUpdateWithOnlyTitle_ShouldReturnUpdatedPost() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();
    User mockUser = payload.user();
    UserProfileDTO mockUserProfile = createMockUserProfile(mockUser);

    String postId = "123";
    UpdatePostDTO invalidDto = UpdatePostDTO.builder()
      .title(JsonNullable.of("Updated Title"))
      .build();

    PostResponseDTO updatedPost = PostResponseDTO.builder()
      .id(postId)
      .title("Updated Title")
      .content("Content")
      .user(mockUserProfile)
      .build();

    when(postsService.updatePost(eq(postId), any(UpdatePostDTO.class), eq(mockUser))).thenReturn(
      updatedPost
    );

    mockMvc
      .perform(
        patch(POSTS_PREFIX + "/" + postId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalidDto))
          .header("Authorization", "Bearer " + mockAccessToken)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(200))
      .andExpect(jsonPath("$.message").isString())
      .andExpect(jsonPath("$.data.id").value(postId))
      .andExpect(jsonPath("$.data.title").value(updatedPost.getTitle()))
      .andExpect(jsonPath("$.data.content").value(updatedPost.getContent()))
      .andExpect(jsonPath("$.data.user.id").value(mockUserProfile.getId()));
  }

  @Test
  void updatePost_WhenUpdateWithOnlyContent_ShouldReturnUpdatedPost() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();
    User mockUser = payload.user();
    UserProfileDTO mockUserProfile = createMockUserProfile(mockUser);

    String postId = "123";
    UpdatePostDTO invalidDto = UpdatePostDTO.builder()
      .content(JsonNullable.of("Updated Content"))
      .build();

    PostResponseDTO updatedPost = PostResponseDTO.builder()
      .id(postId)
      .title("Title")
      .content("Updated Content")
      .user(mockUserProfile)
      .build();

    when(postsService.updatePost(eq(postId), any(UpdatePostDTO.class), eq(mockUser))).thenReturn(
      updatedPost
    );

    mockMvc
      .perform(
        patch(POSTS_PREFIX + "/" + postId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalidDto))
          .header("Authorization", "Bearer " + mockAccessToken)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(200))
      .andExpect(jsonPath("$.message").isString())
      .andExpect(jsonPath("$.data.id").value(postId))
      .andExpect(jsonPath("$.data.title").value(updatedPost.getTitle()))
      .andExpect(jsonPath("$.data.content").value(updatedPost.getContent()))
      .andExpect(jsonPath("$.data.user.id").value(mockUserProfile.getId()));
  }

  @Test
  void updatePost_WhenUpdateWithOnlyIsPublic_ShouldReturnUpdatedPost() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();
    User mockUser = payload.user();
    UserProfileDTO mockUserProfile = createMockUserProfile(mockUser);

    String postId = "123";
    UpdatePostDTO invalidDto = UpdatePostDTO.builder().isPublic(JsonNullable.of(false)).build();

    PostResponseDTO updatedPost = PostResponseDTO.builder()
      .id(postId)
      .title("Title")
      .content("Content")
      .isPublic(false)
      .user(mockUserProfile)
      .build();

    when(postsService.updatePost(eq(postId), any(UpdatePostDTO.class), eq(mockUser))).thenReturn(
      updatedPost
    );

    mockMvc
      .perform(
        patch(POSTS_PREFIX + "/" + postId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalidDto))
          .header("Authorization", "Bearer " + mockAccessToken)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(200))
      .andExpect(jsonPath("$.message").isString())
      .andExpect(jsonPath("$.data.id").value(postId))
      .andExpect(jsonPath("$.data.title").value(updatedPost.getTitle()))
      .andExpect(jsonPath("$.data.content").value(updatedPost.getContent()))
      .andExpect(jsonPath("$.data.isPublic").value(updatedPost.getIsPublic()))
      .andExpect(jsonPath("$.data.user.id").value(mockUserProfile.getId()));
  }

  @Test
  void updatePost_WhenUpdateTitleToNull_ShouldReturnBadRequest() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();

    String postId = "123";
    UpdatePostDTO invalidDto = UpdatePostDTO.builder().title(null).build();

    mockMvc
      .perform(
        patch(POSTS_PREFIX + "/" + postId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalidDto))
          .header("Authorization", "Bearer " + mockAccessToken)
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void updatePost_WhenTitleExceeds255Characters_ShouldReturnBadRequest() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();

    String postId = "123";
    UpdatePostDTO invalidDto = UpdatePostDTO.builder()
      .title(JsonNullable.of("a".repeat(256)))
      .build();

    mockMvc
      .perform(
        patch(POSTS_PREFIX + "/" + postId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalidDto))
          .header("Authorization", "Bearer " + mockAccessToken)
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void updatePost_WhenUpdateContentToNull_ShouldReturnBadRequest() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();

    String postId = "123";
    UpdatePostDTO invalidDto = UpdatePostDTO.builder().content(null).build();

    mockMvc
      .perform(
        patch(POSTS_PREFIX + "/" + postId)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(invalidDto))
          .header("Authorization", "Bearer " + mockAccessToken)
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  private MultiValueMap<String, String> toQueryParams(PostsSearchDTO postsSearchDTO) {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

    if (postsSearchDTO.getPage() != null) params.add("page", postsSearchDTO.getPage().toString());
    if (postsSearchDTO.getPageSize() != null) params.add(
      "pageSize",
      postsSearchDTO.getPageSize().toString()
    );
    if (postsSearchDTO.getOrder() != null && !postsSearchDTO.getOrder().isEmpty()) params.addAll(
      "order",
      postsSearchDTO.getOrder()
    );
    if (postsSearchDTO.getTitle() != null) params.add("title", postsSearchDTO.getTitle());
    if (postsSearchDTO.getUser() != null) params.add("user", postsSearchDTO.getUser());

    return params;
  }
}
