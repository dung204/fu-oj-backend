package com.example.modules.users.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
public class UpdateAvatarDTO {

  @NotNull
  private MultipartFile file;
}
