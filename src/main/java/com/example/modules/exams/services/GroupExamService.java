package com.example.modules.exams.services;

import com.example.modules.exams.dtos.GroupExamRequestDTO;
import com.example.modules.exams.dtos.GroupExamResponseDTO;
import com.example.modules.exams.entities.GroupExam;
import com.example.modules.exams.repositories.GroupExamRepository;
import com.example.modules.exams.utils.GroupExamMapper;
import com.example.modules.exams.utils.GroupExamSpecification;
import com.example.modules.users.entities.User;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupExamService {

  private final GroupExamRepository groupExamRepository;
  private final GroupExamMapper groupExamMapper;

  /**
   * Get group exams với filters
   * - groupExamId: filter theo ID của chính GroupExam
   * - groupId: filter theo group
   * - examId: filter theo exam
   * - ownerId: filter theo owner của group (instructor)
   * - status: filter theo status
   */
  @Transactional(readOnly = true)
  public List<GroupExamResponseDTO> getGroupExams(GroupExamRequestDTO dto, User currentUser) {
    var spec = GroupExamSpecification.builder()
      .withGroupExamId(dto.getGroupExamId())
      .withGroupId(dto.getGroupId())
      .withExamId(dto.getExamId())
      .withOwnerId(dto.getOwnerId())
      .withStatus(dto.getStatus())
      .notDeleted()
      .build();

    List<GroupExam> groupExams = groupExamRepository.findAll(spec);

    log.info(
      "Found {} group exams with filters: groupExamId={}, groupId={}, examId={}, ownerId={}, status={}",
      groupExams.size(),
      dto.getGroupExamId(),
      dto.getGroupId(),
      dto.getExamId(),
      dto.getOwnerId(),
      dto.getStatus()
    );

    return groupExams
      .stream()
      .map(groupExamMapper::toGroupExamResponseDTO)
      .collect(Collectors.toList());
  }
}
