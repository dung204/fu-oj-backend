package com.example.modules.users.controllers;

import static com.example.base.utils.AppRoutes.ME_PREFIX;

import com.example.base.annotations.File;
import com.example.base.dtos.PaginatedSuccessResponseDTO;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.certificates.dtos.UserCertificateResponseDTO;
import com.example.modules.certificates.dtos.UserCertificatesSearchDTO;
import com.example.modules.certificates.services.CertificatesService;
import com.example.modules.users.dtos.UpdateProfileDTO;
import com.example.modules.users.dtos.UserProfileDTO;
import com.example.modules.users.entities.User;
import com.example.modules.users.services.UsersService;
import com.example.modules.users.utils.UserMapper;
import io.minio.errors.MinioException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.util.unit.DataUnit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = ME_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "me", description = "Operations related to the current authenticated users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MeController {

  UsersService usersService;
  UserMapper userMapper;
  CertificatesService certificatesService;

  @Operation(
    summary = "Get profile of current authenticated user",
    responses = {
      @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/profile")
  public SuccessResponseDTO<UserProfileDTO> getProfileOfCurrentUser(@CurrentUser User currentUser) {
    return SuccessResponseDTO.<UserProfileDTO>builder()
      .message("User profile retrieved successfully")
      .data(userMapper.toUserProfileDTO(currentUser))
      .build();
  }

  @Operation(
    summary = "Update profile of current authenticated user",
    responses = {
      @ApiResponse(responseCode = "200", description = "User profile updated successfully"),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PatchMapping("/profile")
  public SuccessResponseDTO<UserProfileDTO> updateUserProfile(
    @CurrentUser User currentUser,
    @RequestBody @Valid UpdateProfileDTO updateProfileDTO
  ) {
    return SuccessResponseDTO.<UserProfileDTO>builder()
      .message("User profile updated successfully")
      .data(usersService.updateProfile(currentUser, updateProfileDTO))
      .build();
  }

  @Operation(
    summary = "Update avatar of current authenticated user",
    responses = {
      @ApiResponse(responseCode = "200", description = "User avatar updated successfully"),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PatchMapping(path = "/avatar", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
  public SuccessResponseDTO<UserProfileDTO> updateUserAvatar(
    @CurrentUser User currentUser,
    @RequestPart("file") @Valid @File(
      maxSize = 1,
      sizeUnit = DataUnit.MEGABYTES,
      allowedTypes = "image/*"
    ) MultipartFile file
  ) throws InvalidKeyException, NoSuchAlgorithmException, MinioException, IOException {
    return SuccessResponseDTO.<UserProfileDTO>builder()
      .message("User avatar updated successfully")
      .data(usersService.updateAvatar(currentUser, file))
      .build();
  }

  @Operation(
    summary = "Retrieve all existing certificates of the current authenticated user",
    responses = {
      @ApiResponse(responseCode = "200", description = "Certificates retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/certificates")
  public PaginatedSuccessResponseDTO<UserCertificateResponseDTO> findAllCertificatesOfCurrentUser(
    @ParameterObject @Valid UserCertificatesSearchDTO userCertificatesSearchDTO,
    @CurrentUser User currentUser
  ) {
    return PaginatedSuccessResponseDTO.<UserCertificateResponseDTO>builder()
      .message("Certificates retrieved successfully.")
      .page(
        certificatesService.findAllCertificatesOfCurrentUser(userCertificatesSearchDTO, currentUser)
      )
      .filters(userCertificatesSearchDTO.getFilters())
      .build();
  }
}
