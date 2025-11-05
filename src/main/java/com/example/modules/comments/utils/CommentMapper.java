package com.example.modules.comments.utils;

import com.example.modules.comments.dtos.CommentResponseDTO;
import com.example.modules.comments.entities.Comment;
import com.example.modules.users.utils.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = { UserMapper.class })
@Slf4j
public abstract class CommentMapper {

  @Named("toCommentResponseDTO")
  @Mapping(target = "user", qualifiedByName = "toUserProfileDTO")
  @Mapping(source = "exercise.id", target = "exerciseId")
  @Mapping(source = "parent.id", target = "parentId")
  public abstract CommentResponseDTO toCommentResponseDTO(Comment comment);
}
