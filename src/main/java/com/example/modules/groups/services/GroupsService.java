package com.example.modules.groups.services;

import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.groups.dtos.GroupRequestDTO;
import com.example.modules.groups.dtos.GroupResponseDTO;
import com.example.modules.groups.dtos.GroupUpdateRequestDTO;
import com.example.modules.groups.entities.Group;
import com.example.modules.groups.exeptions.GroupHasAlreadyBeenUsedException;
import com.example.modules.groups.repositories.GroupsRepository;
import com.example.modules.groups.utils.GroupMapper;
import com.example.modules.users.entities.User;
import com.example.modules.users.repositories.UsersRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class GroupsService {

  private static final String CHARS =
    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final SecureRandom random = new SecureRandom();
  private final GroupsRepository groupsRepository;
  private final UsersRepository usersRepository;
  private final GroupMapper groupMapper;
  private final ExercisesRepository exercisesRepository;

  public GroupsService(
    GroupsRepository groupsRepository,
    UsersRepository usersRepository,
    GroupMapper groupMapper,
    ExercisesRepository exercisesRepository
  ) {
    this.groupsRepository = groupsRepository;
    this.usersRepository = usersRepository;
    this.groupMapper = groupMapper;
    this.exercisesRepository = exercisesRepository;
  }

  //add exercise to group
  public GroupResponseDTO addExerciseToGroup(String groupId, List<String> exerciseIds) {
    Group group = groupsRepository.findGroupById(groupId);
    List<Exercise> exercises = exercisesRepository.findAllById(exerciseIds);
    group.getExercises().addAll(exercises);
    groupsRepository.save(group);
    return groupMapper.toGroupResponseDTO(group);
  }

  //get group by id
  public List<GroupResponseDTO> getGroupByInstructorId(String ownerId) {
    List<Group> groups = groupsRepository.getGroupByOwnerId(ownerId);
    if (groups == null) {
      groups = new ArrayList<>();
    }
    return groupMapper.toGroupResponseDTOList(groups);
  }

  //get all group
  public List<GroupResponseDTO> getGroups() {
    List<Group> groups = groupsRepository.findAll();
    if (groups == null) {
      groups = new ArrayList<>();
    }
    return groupMapper.toGroupResponseDTOList(groups);
  }

  //delete group
  public GroupResponseDTO deleteGroup(String id) {
    Group group = groupsRepository.getGroupById(id);
    group.softDelete();
    groupsRepository.save(group);
    return groupMapper.toGroupResponseDTO(group);
  }

  //update group by id group
  public GroupResponseDTO updateGroup(GroupUpdateRequestDTO requestDTO) {
    Group group = groupsRepository.getGroupById(requestDTO.getId());
    group.setName(requestDTO.getName());
    group.setDescription(requestDTO.getDescription());
    group.setUpdatedTimestamp(Instant.now());
    groupsRepository.save(group);
    return groupMapper.toGroupResponseDTO(group);
  }

  //add group
  public GroupResponseDTO addGroup(GroupRequestDTO groupRequestDTO) {
    final String nameGroup = groupRequestDTO.getName();
    final Optional<Group> existingGroup = groupsRepository.existsGroupByName(nameGroup);
    if (existingGroup.isPresent()) {
      throw new GroupHasAlreadyBeenUsedException();
    }
    User instructor = usersRepository.findUserById(groupRequestDTO.getOwnerId());

    //create group
    Group group = Group.builder()
      .name(groupRequestDTO.getName())
      .description(groupRequestDTO.getDescription())
      .code(generateUniqueClassCode())
      .instructor(instructor)
      .build();
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

  //generate code for class
  public String generateCode(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
    }
    return sb.toString();
  }
}
