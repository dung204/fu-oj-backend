package com.example.modules.topics.services;

import com.example.base.utils.ObjectUtils;
import com.example.modules.topics.dtos.CreateTopicDTO;
import com.example.modules.topics.dtos.TopicResponseDTO;
import com.example.modules.topics.dtos.TopicsSearchDTO;
import com.example.modules.topics.dtos.UpdateTopicDTO;
import com.example.modules.topics.entities.Topic;
import com.example.modules.topics.exceptions.TopicNotFoundException;
import com.example.modules.topics.repositories.TopicsRepository;
import com.example.modules.topics.utils.TopicMapper;
import com.example.modules.topics.utils.TopicsSpecification;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopicsService {

  private final TopicsRepository topicsRepository;
  private final TopicMapper topicMapper;

  public Page<TopicResponseDTO> findAllTopics(TopicsSearchDTO topicsSearchDTO) {
    return topicsRepository
      .findAll(
        TopicsSpecification.builder().containsName(topicsSearchDTO.getName()).notDeleted().build(),
        topicsSearchDTO.toPageRequest()
      )
      .map(topicMapper::toTopicResponseDTO);
  }

  public TopicResponseDTO findTopicById(String id) {
    return topicMapper.toTopicResponseDTO(
      topicsRepository
        .findOne(TopicsSpecification.builder().withId(id).notDeleted().build())
        .orElseThrow(TopicNotFoundException::new)
    );
  }

  public TopicResponseDTO createTopic(CreateTopicDTO createTopicDTO) {
    return topicMapper.toTopicResponseDTO(
      topicsRepository.save(
        Topic.builder()
          .name(createTopicDTO.getName())
          .description(createTopicDTO.getDescription())
          .build()
      )
    );
  }

  public TopicResponseDTO updateTopic(String id, UpdateTopicDTO updateTopicDTO) {
    Topic topic = topicsRepository
      .findOne(TopicsSpecification.builder().notDeleted().withId(id).build())
      .orElseThrow(TopicNotFoundException::new);

    ObjectUtils.assign(topic, updateTopicDTO);
    return topicMapper.toTopicResponseDTO(topicsRepository.save(topic));
  }

  public void deleteTopic(String id) {
    Topic topic = topicsRepository
      .findOne(TopicsSpecification.builder().notDeleted().withId(id).build())
      .orElseThrow(TopicNotFoundException::new);

    topic.setDeletedTimestamp(Instant.now());
    topicsRepository.save(topic);
  }

  public TopicResponseDTO restoreTopic(String id) {
    Topic topic = topicsRepository
      .findOne(TopicsSpecification.builder().deletedOnly().withId(id).build())
      .orElseThrow(TopicNotFoundException::new);

    topic.setDeletedTimestamp(null);
    return topicMapper.toTopicResponseDTO(topicsRepository.save(topic));
  }
}
