package com.example.modules.courses.utils;

import com.example.modules.courses.dtos.CourseResponseDTO;
import com.example.modules.courses.dtos.CourseWithProgressDTO;
import com.example.modules.courses.entities.Course;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class CourseMapper {

  @Named("toCourseResponseDTO")
  public abstract CourseResponseDTO toCourseResponseDTO(Course course);

  @Named("toCourseWithProgressDTO")
  @Mapping(target = "progress", ignore = true)
  public abstract CourseWithProgressDTO toCourseWithProgressDTO(Course course);
}
