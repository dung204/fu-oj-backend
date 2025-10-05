package com.example.modules.groups.services;

import com.example.modules.groups.dtos.GroupRequestDTO;
import com.example.modules.groups.dtos.GroupResponseDTO;
import com.example.modules.groups.entities.Group;
import com.example.modules.groups.exeptions.GroupHasAlreadyBeenUsedException;
import com.example.modules.groups.repositories.GroupsRepository;
import com.example.modules.users.entities.User;
import com.example.modules.users.repositories.UsersRepository;
import java.security.SecureRandom;
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

  public GroupsService(GroupsRepository groupsRepository, UsersRepository usersRepository) {
    this.groupsRepository = groupsRepository;
    this.usersRepository = usersRepository;
  }

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

    //return group => xu ly automapper doan nay
    return GroupResponseDTO.builder()
      .name(groupRequestDTO.getName())
      .description(groupRequestDTO.getDescription())
      .code(generateUniqueClassCode())
      .ownerId(instructor.getId())
      .build();
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
