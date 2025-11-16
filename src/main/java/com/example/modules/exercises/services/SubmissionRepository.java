package com.example.modules.exercises.services;

import com.example.modules.exercises.entities.Exercise;
import com.example.modules.submissions.entities.Submission;
import org.springframework.data.repository.Repository;

interface SubmissionRepository extends Repository<Submission, String> {
  Submission getSubmissionByExercise(Exercise exercise);
}
