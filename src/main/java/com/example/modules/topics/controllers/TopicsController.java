package com.example.modules.topics.controllers;

import static com.example.base.utils.AppRoutes.TOPICS_PREFIX;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = TOPICS_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "topics", description = "Operations related to topics")
@RequiredArgsConstructor
public class TopicsController {}
