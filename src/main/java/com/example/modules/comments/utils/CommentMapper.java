package com.example.modules.comments.utils;

import com.example.modules.comments.dtos.CommentResponseDTO;
import com.example.modules.comments.entities.Comment;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class CommentMapper {

  public abstract CommentResponseDTO toCommentResponseDTO(Comment comment);
}
