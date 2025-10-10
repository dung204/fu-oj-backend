package com.example.modules.exercises.controllers;

import static com.example.base.utils.AppRoutes.EXERCISES_PREFIX;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = EXERCISES_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "exercises", description = "Operations related to exercises")
@RequiredArgsConstructor
public class ExercisesController {}
