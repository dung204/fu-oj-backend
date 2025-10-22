package com.example.modules.groups.services;

import com.example.modules.auth.enums.Role;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.groups.dtos.GroupRequestDTO;
import com.example.modules.groups.dtos.GroupResponseDTO;
import com.example.modules.groups.dtos.GroupUpdateRequestDTO;
import com.example.modules.groups.dtos.GroupsSearchDTO;
import com.example.modules.groups.entities.Group;
import com.example.modules.groups.exeptions.GroupHasAlreadyBeenUsedException;
import com.example.modules.groups.repositories.GroupsRepository;
import com.example.modules.groups.utils.GroupMapper;
import com.example.modules.groups.utils.GroupsSpecification;
import com.example.modules.users.entities.User;
import com.example.modules.users.repositories.UsersRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        break;
    }

    return groupsPage.map(groupMapper::toGroupResponseDTO);
  }

  @Transactional
  public GroupResponseDTO addStudentsToGroup(String groupId, List<String> studentIds) {
    Group group = groupsRepository.findGroupById(groupId);
    List<User> students = usersRepository.findAllById(studentIds);
    if (group.getStudents() == null) {
      group.setStudents(new ArrayList<>());
    }
    group.getStudents().addAll(students);
    groupsRepository.save(group);
    return groupMapper.toGroupResponseDTO(group);
  }

  public List<User> getStudentsByGroupId(String groupId) {
    Group group = groupsRepository.findGroupById(groupId);
    List<User> students = group.getStudents();
    if (students == null) {
      students = new ArrayList<>();
    }
    return students;
  }

  @Transactional
  public GroupResponseDTO removeStudentsFromGroup(String groupId, List<String> studentIds) {
    Group group = groupsRepository.findGroupById(groupId);
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
    Group group = groupsRepository.findGroupById(groupId);
    List<Exercise> exercisesToRemove = group
      .getExercises()
      .stream()
      .filter(exercise -> exerciseIds.contains(exercise.getId()))
      .toList();
    group.getExercises().removeAll(exercisesToRemove);
    groupsRepository.save(group);
    return groupMapper.toGroupResponseDTO(group);
  }

  public List<Exercise> getExerciseByGroupId(String groupId) {
    Group group = groupsRepository.findGroupById(groupId);
    List<Exercise> exercises = group.getExercises();
    if (exercises == null) {
      exercises = new ArrayList<>();
    }
    return exercises;
  }

  @Transactional
  public GroupResponseDTO addExerciseToGroup(String groupId, List<String> exerciseIds) {
    Group group = groupsRepository.findGroupById(groupId);
    List<Exercise> exercises = exercisesRepository.findAllById(exerciseIds);
    group.getExercises().addAll(exercises);
    groupsRepository.save(group);
    return groupMapper.toGroupResponseDTO(group);
  }

  public GroupResponseDTO deleteGroup(String id) {
    Group group = groupsRepository.getGroupById(id);
    group.softDelete();
    groupsRepository.save(group);
    return groupMapper.toGroupResponseDTO(group);
  }

  public GroupResponseDTO updateGroup(String groupId, GroupUpdateRequestDTO requestDTO) {
    Group group = groupsRepository.getGroupById(groupId);
    group.setName(requestDTO.getName());
    group.setDescription(requestDTO.getDescription());
    group.setIsPublic(requestDTO.getIsPublic());
    group.setUpdatedTimestamp(Instant.now());
    groupsRepository.save(group);
    return groupMapper.toGroupResponseDTO(group);
  }

  public GroupResponseDTO addGroup(GroupRequestDTO groupRequestDTO) {
    final String nameGroup = groupRequestDTO.getName();
    final Optional<Group> existingGroup = groupsRepository.existsGroupByName(nameGroup);
    if (existingGroup.isPresent()) {
      throw new GroupHasAlreadyBeenUsedException();
    }
    User instructor = usersRepository.findUserById(groupRequestDTO.getOwnerId());

    Group group = Group.builder()
      .name(groupRequestDTO.getName())
      .description(groupRequestDTO.getDescription())
      .isPublic(groupRequestDTO.isPublic())
      .code(generateUniqueClassCode())
      .instructor(instructor)
      .build();
    if (!groupRequestDTO.isPublic()) {
      group.setCode("");
    }

    groupsRepository.save(group);
    return groupMapper.toGroupResponseDTO(group);
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
