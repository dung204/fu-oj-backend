package com.example.modules.test_cases.controllers;

import static com.example.base.utils.AppRoutes.TEST_CASES_PREFIX;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = TEST_CASES_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "test_cases", description = "Operations related to test cases")
@RequiredArgsConstructor
public class TestCasesController {}
