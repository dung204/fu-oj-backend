package com.example.modules.exams.services;

import com.example.modules.auth.enums.Role;
import com.example.modules.exams.dtos.GroupExamRequestDTO;
import com.example.modules.exams.dtos.GroupExamResponseDTO;
import com.example.modules.exams.entities.GroupExam;
import com.example.modules.exams.exceptions.ExamNotFoundException;
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

  /**
   * Toggle group exam examined status (isExamined field)
   * INSTRUCTOR can only toggle group exams where they own the group
   * ADMIN can toggle any group exam
   */
  @Transactional
  public GroupExamResponseDTO toggleGroupExamExaminedStatus(String id, User currentUser) {
    GroupExam groupExam = groupExamRepository
      .findById(id)
      .orElseThrow(() -> new ExamNotFoundException("Group exam not found"));

    // Check permission: INSTRUCTOR must own the group
    if (currentUser.getAccount().getRole() == Role.INSTRUCTOR) {
      String groupOwnerId = groupExam.getGroup().getInstructor().getId();
      if (!groupOwnerId.equals(currentUser.getId())) {
        throw new ExamNotFoundException("Group exam not found or access denied");
      }
    }

    // Toggle the isExamined status
    groupExam.setIsExamined(!groupExam.getIsExamined());
    GroupExam updatedGroupExam = groupExamRepository.save(groupExam);

    log.info(
      "GroupExam {} examined status toggled to {} by user {}",
      groupExam.getId(),
      updatedGroupExam.getIsExamined(),
      currentUser.getId()
    );

    return groupExamMapper.toGroupExamResponseDTO(updatedGroupExam);
  }
}
