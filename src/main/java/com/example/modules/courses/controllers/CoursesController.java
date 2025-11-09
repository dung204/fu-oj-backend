package com.example.modules.courses.controllers;

import static com.example.base.utils.AppRoutes.COURSES_PREFIX;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = COURSES_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "courses", description = "Operations related to courses")
@RequiredArgsConstructor
public class CoursesController {}
