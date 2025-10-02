package com.example.modules.groups.controllers;

import static com.example.base.utils.AppRoutes.GROUPS_PREFIX;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = GROUPS_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "groups", description = "Operations related to groups")
@RequiredArgsConstructor
public class GroupsController {}
