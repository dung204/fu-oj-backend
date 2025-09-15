package com.example.modules.users.controllers;

import static com.example.base.utils.AppRoutes.USER_PREFIX;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = USER_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "users", description = "Operations related to users")
@RequiredArgsConstructor
public class UsersController {}
