package com.example.modules.groups.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.base.BaseServiceTest;
import com.example.modules.auth.enums.Role;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.exercises.utils.ExerciseMapper;
import com.example.modules.groups.dtos.GroupResponseDTO;
import com.example.modules.groups.dtos.GroupsSearchDTO;
import com.example.modules.groups.dtos.JoinGroupRequestDTO;
import com.example.modules.groups.entities.Group;
import com.example.modules.groups.exeptions.AlreadyJoinedGroupException;
import com.example.modules.groups.exeptions.GroupNotFoundException;
import com.example.modules.groups.repositories.GroupsRepository;
import com.example.modules.groups.utils.GroupMapper;
import com.example.modules.users.entities.User;
import com.example.modules.users.repositories.UsersRepository;
import com.example.modules.users.utils.UserMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

class GroupServiceTest extends BaseServiceTest {

  @Mock
  private GroupsRepository groupsRepository;

  @Mock
  private UsersRepository usersRepository;

  @Mock
  private GroupMapper groupMapper;

  @Mock
  private ExercisesRepository exercisesRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private ExerciseMapper exerciseMapper;

  @InjectMocks
  private GroupService groupService;

  private Group sampleGroup;

  @BeforeEach
  void setUp() {
    User instructor = getMockUser();
    instructor.getAccount().setRole(Role.INSTRUCTOR);

    sampleGroup = Group.builder()
      .id("group-1")
      .code("ABC12345")
      .name("Algorithms")
      .description("DSA group")
      .isPublic(true)
      .instructor(instructor)
      .students(new ArrayList<>())
      .exercises(new ArrayList<>())
      .build();
  }

  @Test
  void getGroups_WhenAdminRole_ShouldReturnMappedDtos() {
    User admin = getMockUser();
    admin.getAccount().setRole(Role.ADMIN);
    GroupsSearchDTO searchDTO = new GroupsSearchDTO();

    Page<Group> groupsPage = new PageImpl<>(List.of(sampleGroup));
    when(groupsRepository.findAll(any(), any())).thenReturn(groupsPage);
    when(groupMapper.toGroupResponseDTO(sampleGroup)).thenReturn(
      GroupResponseDTO.builder().id("group-1").build()
    );

    Page<GroupResponseDTO> result = groupService.getGroups(admin, searchDTO);

    assertEquals(1, result.getTotalElements());
    assertEquals("group-1", result.getContent().get(0).getId());
    verify(groupsRepository).findAll(any(), any());
  }

  @Test
  void getGroupById_WhenStudentJoined_ShouldMarkJoinedFlag() {
    User student = getMockUser();
    student.getAccount().setRole(Role.STUDENT);
    sampleGroup.getStudents().add(student);
    GroupResponseDTO responseDTO = GroupResponseDTO.builder().id("group-1").joined(null).build();

    when(groupsRepository.findOne(any())).thenReturn(Optional.of(sampleGroup));
    when(groupMapper.toGroupResponseDTO(sampleGroup)).thenReturn(responseDTO);

    GroupResponseDTO result = groupService.getGroupById("group-1", student);

    assertEquals(Boolean.TRUE, result.getJoined());
  }

  @Test
  void joinGroupByCode_WhenAlreadyJoined_ShouldThrowException() {
    User student = getMockUser();
    student.getAccount().setRole(Role.STUDENT);
    sampleGroup.getStudents().add(student);
    when(groupsRepository.findOne(any())).thenReturn(Optional.of(sampleGroup));

    JoinGroupRequestDTO joinRequest = JoinGroupRequestDTO.builder().code("ABC12345").build();

    assertThrows(AlreadyJoinedGroupException.class, () ->
      groupService.joinGroupByCode(student, joinRequest)
    );
  }

  @Test
  void joinGroupByCode_WhenGroupMissing_ShouldThrowNotFound() {
    User student = getMockUser();
    when(groupsRepository.findOne(any())).thenReturn(Optional.empty());

    JoinGroupRequestDTO joinRequest = JoinGroupRequestDTO.builder().code("NOT_FOUND").build();

    assertThrows(GroupNotFoundException.class, () ->
      groupService.joinGroupByCode(student, joinRequest)
    );
  }

  @Test
  void generateUniqueClassCode_ShouldRetryUntilUnique() {
    when(groupsRepository.existsGroupByCode(any())).thenReturn(true, false);

    String code = groupService.generateUniqueClassCode();

    assertEquals(8, code.length());
    verify(groupsRepository, times(2)).existsGroupByCode(any());
  }

  @Test
  void addStudentsToGroup_WhenEmailsProvided_ShouldAppendStudents() {
    User student = getMockUser();
    sampleGroup.getStudents().clear();
    List<User> newStudents = List.of(student);
    when(groupsRepository.findGroupById("group-1")).thenReturn(Optional.of(sampleGroup));
    when(usersRepository.findAllByAccount_EmailIn(anyList())).thenReturn(newStudents);
    when(groupMapper.toGroupResponseDTO(sampleGroup)).thenReturn(
      GroupResponseDTO.builder().id("group-1").studentsCount(1).build()
    );

    GroupResponseDTO response = groupService.addStudentsToGroup(
      "group-1",
      List.of("mail@test.com")
    );

    assertEquals(1, sampleGroup.getStudents().size());
    assertEquals("group-1", response.getId());
    verify(groupsRepository).save(sampleGroup);
  }
}
