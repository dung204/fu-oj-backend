package com.example.modules.users.services;

import com.example.base.utils.ObjectUtils;
import com.example.modules.minio.dtos.MinioFileResponse;
import com.example.modules.minio.services.MinioService;
import com.example.modules.users.dtos.UpdateProfileDTO;
import com.example.modules.users.dtos.UserProfileDTO;
import com.example.modules.users.entities.User;
import com.example.modules.users.repositories.UsersRepository;
import com.example.modules.users.utils.UserMapper;
import io.minio.errors.MinioException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsersService {

  private final UsersRepository usersRepository;
  private final MinioService minioService;
  private final UserMapper userMapper;

  public UserProfileDTO updateProfile(User user, UpdateProfileDTO updateProfileDTO) {
    ObjectUtils.assign(user, updateProfileDTO);
    User savedUser = usersRepository.save(user);

    return userMapper.toUserProfileDTO(savedUser);
  }

  public UserProfileDTO updateAvatar(User user, MultipartFile file)
    throws InvalidKeyException, NoSuchAlgorithmException, MinioException, IOException {
    MinioFileResponse payload = minioService.uploadFile(file, "avatars/%s".formatted(user.getId()));

    user.setAvatar(payload.getFileName());
    User savedUser = usersRepository.save(user);

    return userMapper.toUserProfileDTO(savedUser);
  }
}
