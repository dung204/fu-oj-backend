package com.example.modules.comments.controllers;

import static com.example.base.utils.AppRoutes.COMMENTS_PREFIX;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = COMMENTS_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "comments", description = "Operations related to comments")
@RequiredArgsConstructor
public class CommentsController {}
