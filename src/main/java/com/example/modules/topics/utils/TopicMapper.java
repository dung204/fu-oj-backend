package com.example.modules.topics.utils;

import com.example.modules.topics.dtos.TopicResponseDTO;
import com.example.modules.topics.entities.Topic;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class TopicMapper {

  public abstract TopicResponseDTO toTopicResponseDTO(Topic topic);
}
