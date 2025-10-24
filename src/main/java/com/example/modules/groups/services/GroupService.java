package com.example.modules.groups.services;

import com.example.modules.auth.enums.Role;
import com.example.modules.exercises.dtos.ExerciseQueryDTO;
import com.example.modules.exercises.dtos.ExerciseResponseDTO;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.exercises.utils.ExerciseMapper;
import com.example.modules.exercises.utils.ExercisesSpecification;
import com.example.modules.groups.dtos.GroupRequestDTO;
import com.example.modules.groups.dtos.GroupResponseDTO;
import com.example.modules.groups.dtos.GroupUpdateRequestDTO;
import com.example.modules.groups.dtos.GroupsSearchDTO;
import com.example.modules.groups.dtos.JoinGroupRequestDTO;
import com.example.modules.groups.entities.Group;
import com.example.modules.groups.exeptions.AlreadyJoinedGroupException;
import com.example.modules.groups.exeptions.GroupNotFoundException;
import com.example.modules.groups.repositories.GroupsRepository;
import com.example.modules.groups.utils.GroupMapper;
import com.example.modules.groups.utils.GroupsSpecification;
import com.example.modules.users.dtos.StudentsSearchDTO;
import com.example.modules.users.dtos.UserProfileDTO;
import com.example.modules.users.entities.User;
import com.example.modules.users.repositories.UsersRepository;
import com.example.modules.users.utils.UserMapper;
import com.example.modules.users.utils.UsersSpecification;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupService {

  private static final String CHARS =
    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final SecureRandom random = new SecureRandom();

  private final GroupsRepository groupsRepository;
  private final UsersRepository usersRepository;
  private final GroupMapper groupMapper;
  private final ExercisesRepository exercisesRepository;
  private final UserMapper userMapper;
  private final ExerciseMapper exerciseMapper;

  public Page<GroupResponseDTO> getGroups(User currentUser, GroupsSearchDTO groupsSearchDTO) {
    Page<Group> groupsPage = null;

    switch (currentUser.getAccount().getRole()) {
      case Role.ADMIN:
        groupsPage = groupsRepository.findAll(
          GroupsSpecification.builder()
            .containsName(groupsSearchDTO.getName())
            .conditionally(
              groupsSearchDTO.getIsPublic() != null && groupsSearchDTO.getIsPublic(),
              GroupsSpecification::publicOnly
            )
            .conditionally(
              groupsSearchDTO.getIsPublic() != null && !groupsSearchDTO.getIsPublic(),
              GroupsSpecification::privateOnly
            )
            .notDeleted()
            .build(),
          groupsSearchDTO.toPageRequest()
        );
        break;
      case Role.INSTRUCTOR:
        groupsPage = groupsRepository.findAll(
          GroupsSpecification.builder()
            .ownedBy(currentUser.getId())
            .containsName(groupsSearchDTO.getName())
            .conditionally(
              groupsSearchDTO.getIsPublic() != null && groupsSearchDTO.getIsPublic(),
              GroupsSpecification::publicOnly
            )
            .conditionally(
              groupsSearchDTO.getIsPublic() != null && !groupsSearchDTO.getIsPublic(),
              GroupsSpecification::privateOnly
            )
            .notDeleted()
            .build(),
          groupsSearchDTO.toPageRequest()
        );
        break;
      case Role.STUDENT:
        groupsPage = groupsRepository.findAll(
          GroupsSpecification.builder()
            .containsName(groupsSearchDTO.getName())
            .<GroupsSpecification>conditionally(
              "joined".equals(groupsSearchDTO.getFilter()),
              spec -> spec.joinedBy(currentUser.getId()),
              GroupsSpecification::publicOnly
            )
            .notDeleted()
            .build(),
          groupsSearchDTO.toPageRequest()
        );

        return groupsPage.map(group -> {
          GroupResponseDTO dto = groupMapper.toGroupResponseDTO(group);
          dto.setJoined(group.getStudents().contains(currentUser));
          return dto;
        });
    }

    return groupsPage.map(groupMapper::toGroupResponseDTO);
  }

  public GroupResponseDTO getGroupById(String id, User currentUser) {
    Group group = groupsRepository
      .findOne(
        GroupsSpecification.builder()
          .withId(id)
          .<GroupsSpecification>conditionally(
            currentUser.getAccount().getRole() == Role.STUDENT,
            spec -> spec.joinedByOrPublicOnly(currentUser.getId())
          )
          .<GroupsSpecification>conditionally(
            currentUser.getAccount().getRole() == Role.INSTRUCTOR,
            spec -> spec.ownedByOrPublicOnly(currentUser.getId())
          )
          .notDeleted()
          .build()
      )
      .orElseThrow(GroupNotFoundException::new);
    GroupResponseDTO responseDTO = groupMapper.toGroupResponseDTO(group);

    if (currentUser.getAccount().getRole().equals(Role.STUDENT)) {
      responseDTO.setJoined(group.getStudents().contains(currentUser));
    }

    return responseDTO;
  }

  @Transactional
  public GroupResponseDTO addStudentsToGroup(String groupId, List<String> studentIds) {
    Group group = groupsRepository.findGroupById(groupId).orElseThrow(GroupNotFoundException::new);
    List<User> students = usersRepository.findAllById(studentIds);
    if (group.getStudents() == null) {
      group.setStudents(new ArrayList<>());
    }
    group.getStudents().addAll(students);
    groupsRepository.save(group);
    return groupMapper.toGroupResponseDTO(group);
  }

  public Page<UserProfileDTO> getStudentsByGroupId(
    String groupId,
    StudentsSearchDTO studentsSearchDTO,
    User currentUser
  ) {
    // If the group is not found or currentUser does not have access to the group,
    // throw a 404 Not Found
    getGroupById(groupId, currentUser);

    return usersRepository
      .findAll(
        UsersSpecification.builder()
          .inGroup(groupId)
          .containsFullNameOrContainsRollNumberOrContainsEmail(studentsSearchDTO.getQuery())
          .notDeleted()
          .build(),
        studentsSearchDTO.toPageRequest()
      )
      .map(userMapper::toUserProfileDTO);
  }

  @Transactional
  public GroupResponseDTO removeStudentsFromGroup(String groupId, List<String> studentIds) {
    Group group = groupsRepository.findGroupById(groupId).orElseThrow(GroupNotFoundException::new);
    List<User> studentsToRemove = group
      .getStudents()
      .stream()
      .filter(student -> studentIds.contains(student.getId()))
      .toList();

    group.getStudents().removeAll(studentsToRemove);
    groupsRepository.save(group);
    return groupMapper.toGroupResponseDTO(group);
  }

  @Transactional
  public GroupResponseDTO removeExercisesFromGroup(String groupId, List<String> exerciseIds) {
    Group group = groupsRepository.findGroupById(groupId).orElseThrow(GroupNotFoundException::new);
    List<Exercise> exercisesToRemove = group
      .getExercises()
      .stream()
      .filter(exercise -> exerciseIds.contains(exercise.getId()))
      .toList();
    group.getExercises().removeAll(exercisesToRemove);
    groupsRepository.save(group);
    return groupMapper.toGroupResponseDTO(group);
  }

  public Page<ExerciseResponseDTO> getExercisesByGroupId(
    String groupId,
    ExerciseQueryDTO queryDTO,
    User currentUser
  ) {
    getGroupById(groupId, currentUser);

    return exercisesRepository
      .findAll(
        ExercisesSpecification.builder()
          .withGroupId(groupId)
          .containsCodeOrContainsTitle(queryDTO.getQuery())
          .hasOneOfTopics(queryDTO.getTopic())
          .build(),
        queryDTO.toPageRequest()
      )
      .map(exerciseMapper::toExerciseResponseDTOWithPrivateTestCasesHidden);
  }

  @Transactional
  public GroupResponseDTO addExerciseToGroup(String groupId, List<String> exerciseIds) {
    Group group = groupsRepository.findGroupById(groupId).orElseThrow(GroupNotFoundException::new);
    List<Exercise> exercises = exercisesRepository.findAllById(exerciseIds);
    group.getExercises().addAll(exercises);
    groupsRepository.save(group);
    return groupMapper.toGroupResponseDTO(group);
  }

  public GroupResponseDTO deleteGroup(String id) {
    Group group = groupsRepository.findGroupById(id).orElseThrow(GroupNotFoundException::new);
    group.softDelete();
    groupsRepository.save(group);
    return groupMapper.toGroupResponseDTO(group);
  }

  public GroupResponseDTO updateGroup(String groupId, GroupUpdateRequestDTO requestDTO) {
    Group group = groupsRepository.findGroupById(groupId).orElseThrow(GroupNotFoundException::new);
    group.setName(requestDTO.getName());
    group.setDescription(requestDTO.getDescription());
    group.setIsPublic(requestDTO.getIsPublic());
    group.setUpdatedTimestamp(Instant.now());
    groupsRepository.save(group);
    return groupMapper.toGroupResponseDTO(group);
  }

  public GroupResponseDTO addGroup(User currentUser, GroupRequestDTO groupRequestDTO) {
    Group group = Group.builder()
      .name(groupRequestDTO.getName())
      .description(groupRequestDTO.getDescription())
      .isPublic(groupRequestDTO.isPublic())
      .code(generateUniqueClassCode())
      .instructor(currentUser)
      .build();

    groupsRepository.save(group);
    GroupResponseDTO responseDTO = groupMapper.toGroupResponseDTO(group);
    responseDTO.setJoined(true);

    return responseDTO;
  }

  public GroupResponseDTO joinGroupByCode(User currentUser, JoinGroupRequestDTO dto) {
    Group group = this.groupsRepository.findOne(
      GroupsSpecification.builder().withCode(dto.getCode()).notDeleted().build()
    ).orElseThrow(GroupNotFoundException::new);

    if (group.getStudents().contains(currentUser)) {
      throw new AlreadyJoinedGroupException();
    }

    group.getStudents().add(currentUser);
    return groupMapper.toGroupResponseDTO(groupsRepository.save(group));
  }

  @Transactional
  public String generateUniqueClassCode() {
    String code;
    do {
      code = generateCode(8);
    } while (groupsRepository.existsGroupByCode(code));
    return code;
  }

  public String generateCode(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
    }
    return sb.toString();
  }
}
