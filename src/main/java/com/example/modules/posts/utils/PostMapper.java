package com.example.modules.posts.utils;

import com.example.modules.posts.dtos.PostResponseDTO;
import com.example.modules.posts.entities.Post;
import com.example.modules.users.utils.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public abstract class PostMapper {

  @Named("toPostResponseDTO")
  @Mapping(source = "user", target = "user", qualifiedByName = "toUserProfileDTO")
  public abstract PostResponseDTO toPostResponseDTO(Post post);
}
