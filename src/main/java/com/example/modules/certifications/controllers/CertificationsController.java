package com.example.modules.certifications.controllers;

import static com.example.base.utils.AppRoutes.CERTIFICATIONS_PREFIX;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = CERTIFICATIONS_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "certifications", description = "Operations related to certifications")
@RequiredArgsConstructor
public class CertificationsController {}
