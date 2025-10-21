package com.example.modules.groups.services;

import com.example.modules.exercises.entities.Exercise;
import com.example.modules.groups.dtos.GroupRequestDTO;
import com.example.modules.groups.dtos.GroupResponseDTO;
import com.example.modules.groups.dtos.GroupUpdateRequestDTO;
import com.example.modules.users.entities.User;
import java.util.List;

public interface GroupsService {
  GroupResponseDTO addStudentsToGroup(String groupId, List<String> studentIds);

  List<User> getStudentsByGroupId(String groupId);

  GroupResponseDTO removeStudentsFromGroup(String groupId, List<String> studentIds);

  GroupResponseDTO removeExercisesFromGroup(String groupId, List<String> exerciseIds);

  List<Exercise> getExerciseByGroupId(String groupId);

  GroupResponseDTO addExerciseToGroup(String groupId, List<String> exerciseIds);

  List<GroupResponseDTO> getGroupByInstructorId(String ownerId);

  List<GroupResponseDTO> getGroups();

  GroupResponseDTO deleteGroup(String id);

  GroupResponseDTO updateGroup(GroupUpdateRequestDTO requestDTO);

  GroupResponseDTO addGroup(GroupRequestDTO groupRequestDTO);

  String generateUniqueClassCode();

  String generateCode(int length);
}
