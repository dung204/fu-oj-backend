package com.example.modules.ai.service;

import com.example.modules.ai.dtos.request.ChatRequest;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.exceptions.ExerciseNotFoundException;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.test_cases.entities.TestCase;
import com.example.modules.test_cases.repositories.TestCasesRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class ChatService {

  private static final int MAX_TEST_CASES = 3;
  private static final int MAX_SOLUTION_LENGTH = 1200;
  private static final String BASE_TUTOR_PROMPT =
    "You are FU-OJ Algorithm Tutor AI — a specialized teaching assistant focused on guiding learners through problem-solving step by step rather than giving direct answers.\n" +
    "\n" +
    "Your primary goal is to teach *how to think*, not just *what to code*.\n" +
    "\n" +
    "Behavior rules:\n" +
    "\n" +
    "1. Start every session by:\n" +
    "   - Restating the problem in simple words.\n" +
    "   - Asking the learner what they already understand and where they feel stuck.\n" +
    "\n" +
    "2. Clearly list the prerequisite concepts required to solve the exercise\n" +
    "   (e.g. loops, arrays, recursion, hash map, prefix sum, two pointers…).\n" +
    "   If a concept is missing, briefly explain it before moving on.\n" +
    "\n" +
    "3. Guide the learner through a structured problem-solving process:\n" +
    "   - Step 1: Analyze input/output and constraints.\n" +
    "   - Step 2: Identify patterns or brute-force ideas.\n" +
    "   - Step 3: Optimize step by step (why brute force fails, what can be improved).\n" +
    "   - Step 4: Design the algorithm logic in plain language.\n" +
    "   - Step 5: Translate logic into pseudocode (before real code).\n" +
    "\n" +
    "4. Use hints instead of solutions:\n" +
    "   - Ask guiding questions.\n" +
    "   - Give partial ideas, diagrams, or small examples.\n" +
    "   - Encourage the learner to fill in missing steps.\n" +
    "\n" +
    "5. Encourage active participation:\n" +
    "   - Ask the learner to try a partial solution or explain their idea.\n" +
    "   - Review their logic or code and give constructive feedback.\n" +
    "   - Point out mistakes gently and explain *why* they are wrong.\n" +
    "\n" +
    "6. Only provide a full solution if:\n" +
    "   - The learner explicitly asks for it, or\n" +
    "   - The learner is clearly stuck after multiple guided attempts.\n" +
    "\n" +
    "7. When giving a full solution:\n" +
    "   - Explain each line’s purpose.\n" +
    "   - Analyze time and space complexity.\n" +
    "   - Discuss edge cases and common pitfalls.\n" +
    "\n" +
    "8. Keep the tone:\n" +
    "   - Patient, supportive, and encouraging.\n" +
    "   - Clear, structured, and beginner-friendly.\n" +
    "   - Never condescending or rushed.\n" +
    "\n" +
    "9. Strict rule:\n" +
    "   - Use only the provided exercise context as the knowledge base.\n" +
    "   - Never assume or fabricate missing details.\n" +
    "\n" +
    "Your success is measured by whether the learner understands the reasoning process and can apply it to similar problems.\n";

  private final ChatClient chatClient;
  private final ExercisesRepository exercisesRepository;
  private final TestCasesRepository testCasesRepository;

  public ChatService(
    ChatClient.Builder builder,
    ExercisesRepository exercisesRepository,
    TestCasesRepository testCasesRepository
  ) {
    this.chatClient = builder.build();
    this.exercisesRepository = exercisesRepository;
    this.testCasesRepository = testCasesRepository;
  }

  @Transactional(readOnly = true)
  public String chat(ChatRequest chatRequest) {
    String exerciseContext = buildExerciseContext(chatRequest.getExerciseId());
    try {
      SystemMessage systemMessage = new SystemMessage(buildSystemPrompt(exerciseContext));
      UserMessage userMessage = new UserMessage(chatRequest.getMessage());

      Prompt prompt = new Prompt(systemMessage, userMessage);
      return chatClient.prompt(prompt).call().content();
    } catch (NonTransientAiException e) {
      log.error("AI service error: {}", e.getMessage(), e);

      return "Xin lỗi, đã xảy ra lỗi khi xử lý yêu cầu của bạn. Vui lòng thử lại sau.";
    } catch (Exception e) {
      log.error("Unexpected error in chat service: {}", e.getMessage(), e);
      return "Xin lỗi, đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau.";
    }
  }

  private String buildSystemPrompt(String exerciseContext) {
    if (!StringUtils.hasText(exerciseContext)) {
      return (
        BASE_TUTOR_PROMPT +
        "\n\nExercise context: Not provided. Ask focused questions to understand the learner's exercise."
      );
    }
    return (
      BASE_TUTOR_PROMPT +
      "\n\nExercise context retrieved from FU-OJ knowledge base:\n" +
      exerciseContext
    );
  }

  private String buildExerciseContext(String exerciseId) {
    if (!StringUtils.hasText(exerciseId)) {
      return null;
    }

    Exercise exercise = exercisesRepository
      .findById(exerciseId)
      .orElseThrow(() ->
        new ExerciseNotFoundException("Không tìm thấy bài tập với id %s".formatted(exerciseId))
      );

    String topics = CollectionUtils.isEmpty(exercise.getTopics())
      ? "Unspecified"
      : exercise
        .getTopics()
        .stream()
        .map(topic -> topic.getName())
        .collect(Collectors.joining(", "));

    List<TestCase> publicTestCases = testCasesRepository.findAllByExerciseIdAndIsPublicTrue(
      exerciseId
    );

    StringBuilder contextBuilder = new StringBuilder()
      .append("Title: ")
      .append(exercise.getTitle())
      .append("\nDifficulty: ")
      .append(exercise.getDifficulty())
      .append("\nTopics: ")
      .append(topics)
      .append("\nVisibility: ")
      .append(exercise.getVisibility())
      .append("\nConstraints: time limit ")
      .append(exercise.getTimeLimit())
      .append("s, memory ")
      .append(exercise.getMemory())
      .append("KB, max submissions ")
      .append(exercise.getMaxSubmissions())
      .append("\nProblem statement:\n")
      .append(exercise.getDescription())
      .append("\n");

    if (StringUtils.hasText(exercise.getSolution())) {
      contextBuilder
        .append("Reference solution outline (truncated):\n")
        .append(truncate(exercise.getSolution(), MAX_SOLUTION_LENGTH))
        .append("\n");
    }

    if (CollectionUtils.isEmpty(publicTestCases)) {
      contextBuilder.append("No public sample test cases were provided.\n");
    } else {
      contextBuilder.append("Public sample test cases:\n");
      publicTestCases
        .stream()
        .limit(MAX_TEST_CASES)
        .forEach(testCase -> {
          contextBuilder
            .append("- Input: ")
            .append(safeText(testCase.getInput()))
            .append("\n  Expected output: ")
            .append(safeText(testCase.getOutput()))
            .append("\n");
          if (StringUtils.hasText(testCase.getNote())) {
            contextBuilder.append("  Note: ").append(testCase.getNote()).append("\n");
          }
        });
    }

    return contextBuilder.toString();
  }

  private String truncate(String content, int limit) {
    if (!StringUtils.hasText(content) || content.length() <= limit) {
      return content;
    }
    return content.substring(0, limit) + "...";
  }

  private String safeText(String value) {
    return StringUtils.hasText(value) ? value : "N/A";
  }
}
