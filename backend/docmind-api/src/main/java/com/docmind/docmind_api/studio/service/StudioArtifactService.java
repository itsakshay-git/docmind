package com.docmind.docmind_api.studio.service;

import com.docmind.docmind_api.notebook.repository.NotebookRepository;
import com.docmind.docmind_api.rag.dto.SemanticSearchRequest;
import com.docmind.docmind_api.rag.dto.SemanticSearchResult;
import com.docmind.docmind_api.rag.service.RagSearchService;
import com.docmind.docmind_api.studio.dto.GenerateStudioArtifactRequest;
import com.docmind.docmind_api.studio.dto.StudioArtifactResponse;
import com.docmind.docmind_api.studio.entity.StudioArtifact;
import com.docmind.docmind_api.studio.enums.StudioArtifactType;
import com.docmind.docmind_api.studio.repository.StudioArtifactRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudioArtifactService {

    private static final int STUDIO_TOP_K = 12;

    private final NotebookRepository notebookRepository;
    private final RagSearchService ragSearchService;
    private final StudioArtifactRepository studioArtifactRepository;
    private final ChatModel chatModel;
    private final GeminiTtsService geminiTtsService;
    private final InfographicImageRenderer infographicImageRenderer;
    private final ObjectMapper objectMapper;

    @Value("${docmind.studio.audio-storage-dir}")
    private String audioStorageDir;

    @Value("${docmind.studio.image-storage-dir}")
    private String imageStorageDir;

    @Transactional(readOnly = true)
    public List<StudioArtifactResponse> listArtifacts(
            UUID notebookId,
            String ownerEmail
    ) {

        notebookRepository.findByIdAndOwnerEmail(
                notebookId,
                ownerEmail
        ).orElseThrow();

        return studioArtifactRepository
                .findByNotebookIdAndOwnerEmailOrderByCreatedAtDesc(
                        notebookId,
                        ownerEmail
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudioArtifactResponse getArtifact(
            UUID artifactId,
            String ownerEmail
    ) {

        return toResponse(
                getOwnedArtifact(
                        artifactId,
                        ownerEmail
                )
        );
    }

    @Transactional
    public StudioArtifactResponse generateArtifact(
            UUID notebookId,
            GenerateStudioArtifactRequest request,
            String ownerEmail
    ) {

        notebookRepository.findByIdAndOwnerEmail(
                notebookId,
                ownerEmail
        ).orElseThrow();

        StudioArtifactType type =
                normalizeType(
                        request.getType()
                );

        List<SemanticSearchResult> context =
                retrieveContext(
                        notebookId,
                        ownerEmail,
                        type
                );

        if (context.isEmpty()) {
            throw new RuntimeException(
                    "Add sources before generating Studio artifacts."
            );
        }

        JsonNode generated =
                generateWithGemini(
                        type,
                        request.getInstruction(),
                        context
                );

        StudioArtifact artifact =
                new StudioArtifact();

        artifact.setNotebookId(notebookId);
        artifact.setOwnerEmail(ownerEmail);
        artifact.setType(type);
        artifact.setTitle(requiredText(generated, "title"));
        artifact.setMarkdownContent(requiredText(generated, "markdownContent"));
        JsonNode data =
                requiredNode(
                        generated,
                        "data"
                );

        validateArtifactData(
                type,
                data
        );

        artifact.setJsonContent(toPrettyJson(data));
        artifact.setSourceChunkIds(
                toPrettyJson(
                        context.stream()
                                .map(SemanticSearchResult::getChunkId)
                                .toList()
                )
        );

        if (type == StudioArtifactType.PODCAST_SCRIPT) {
            generateAudioIfPossible(
                    artifact
            );
        }

        if (type == StudioArtifactType.INFOGRAPHIC_OUTLINE) {
            generateInfographicImage(
                    artifact,
                    data
            );
        }

        return toResponse(
                studioArtifactRepository.save(
                        artifact
                )
        );
    }

    @Transactional
    public void deleteArtifact(
            UUID artifactId,
            String ownerEmail
    ) {

        StudioArtifact artifact =
                getOwnedArtifact(
                        artifactId,
                        ownerEmail
                );

        deleteAudioFile(
                artifact
        );

        deleteImageFile(
                artifact
        );

        studioArtifactRepository.delete(
                artifact
        );
    }

    @Transactional(readOnly = true)
    public StudioArtifact getOwnedArtifact(
            UUID artifactId,
            String ownerEmail
    ) {

        return studioArtifactRepository
                .findByIdAndOwnerEmail(
                        artifactId,
                        ownerEmail
                )
                .orElseThrow();
    }

    private StudioArtifactType normalizeType(
            StudioArtifactType type
    ) {

        if (type == null) {
            throw new IllegalArgumentException(
                    "Studio artifact type is required"
            );
        }

        return type;
    }

    private List<SemanticSearchResult> retrieveContext(
            UUID notebookId,
            String ownerEmail,
            StudioArtifactType type
    ) {

        SemanticSearchRequest searchRequest =
                new SemanticSearchRequest();

        searchRequest.setQuestion(
                retrievalQuery(type)
        );

        searchRequest.setTopK(
                STUDIO_TOP_K
        );

        return ragSearchService.search(
                notebookId,
                searchRequest,
                ownerEmail
        );
    }

    private String retrievalQuery(
            StudioArtifactType type
    ) {

        return switch (type) {
            case FLASHCARDS -> "key concepts, definitions, facts, terminology for 6 useful flashcards";
            case QUIZ -> "important testable ideas and misconceptions for an 8 question quiz";
            case BRIEFING -> "summary, main ideas, decisions, examples";
            case PODCAST_SCRIPT -> "major topics, narrative flow, examples for a two-host podcast";
            case INFOGRAPHIC_OUTLINE -> "entities, relationships, timeline, process, hierarchy";
        };
    }

    private JsonNode generateWithGemini(
            StudioArtifactType type,
            String instruction,
            List<SemanticSearchResult> context
    ) {

        String response =
                chatModel.call(
                        buildPrompt(
                                type,
                                instruction,
                                context
                        )
                );

        try {
            return objectMapper.readTree(
                    stripJsonFence(response)
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse Studio artifact response",
                    e
            );
        }
    }

    private String buildPrompt(
            StudioArtifactType type,
            String instruction,
            List<SemanticSearchResult> context
    ) {

        String contextText =
                context.stream()
                        .map(result -> "Source passage\nContent:\n"
                                + result.getContent())
                        .collect(
                                Collectors.joining(
                                        "\n\n---\n\n"
                                )
                        );

        return """
                You are DocMind Studio, a source-grounded study artifact generator.
                Use only the provided notebook context.
                Do not invent facts beyond the context.
                Do not include UUIDs, chunk IDs, document IDs, scores, or implementation details in markdownContent.
                Return only valid JSON. Do not wrap it in Markdown fences.
                Make the artifact substantial enough to be useful, not a generic summary.
                Flashcards must include exactly 6 cards. Quiz must include exactly 8 questions.
                Infographic data must include a title and at least 3 sections with short visual text blocks.

                Artifact type: %s
                Optional user instruction: %s

                Required outer JSON shape:
                {
                  "title": "short artifact title",
                  "markdownContent": "human-readable Markdown preview",
                  "data": %s
                }

                Notebook context:
                %s
                """.formatted(
                type,
                normalizeInstruction(instruction),
                dataShape(type),
                contextText
        );
    }

    private String normalizeInstruction(
            String instruction
    ) {

        if (instruction == null || instruction.isBlank()) {
            return "No additional instruction.";
        }

        return instruction.trim();
    }

    private String dataShape(
            StudioArtifactType type
    ) {

        return switch (type) {
            case FLASHCARDS -> """
                    {
                      "cards": [
                        { "front": "...", "back": "...", "difficulty": "basic|medium|advanced" },
                        { "front": "...", "back": "...", "difficulty": "basic|medium|advanced" },
                        { "front": "...", "back": "...", "difficulty": "basic|medium|advanced" },
                        { "front": "...", "back": "...", "difficulty": "basic|medium|advanced" },
                        { "front": "...", "back": "...", "difficulty": "basic|medium|advanced" },
                        { "front": "...", "back": "...", "difficulty": "basic|medium|advanced" }
                      ]
                    }
                    """;
            case QUIZ -> """
                    {
                      "questions": [
                        {
                          "question": "...",
                          "options": ["A", "B", "C", "D"],
                          "answer": "...",
                          "explanation": "..."
                        },
                        {
                          "question": "...",
                          "options": ["A", "B", "C", "D"],
                          "answer": "...",
                          "explanation": "..."
                        },
                        {
                          "question": "...",
                          "options": ["A", "B", "C", "D"],
                          "answer": "...",
                          "explanation": "..."
                        },
                        {
                          "question": "...",
                          "options": ["A", "B", "C", "D"],
                          "answer": "...",
                          "explanation": "..."
                        },
                        {
                          "question": "...",
                          "options": ["A", "B", "C", "D"],
                          "answer": "...",
                          "explanation": "..."
                        },
                        {
                          "question": "...",
                          "options": ["A", "B", "C", "D"],
                          "answer": "...",
                          "explanation": "..."
                        },
                        {
                          "question": "...",
                          "options": ["A", "B", "C", "D"],
                          "answer": "...",
                          "explanation": "..."
                        },
                        {
                          "question": "...",
                          "options": ["A", "B", "C", "D"],
                          "answer": "...",
                          "explanation": "..."
                        }
                      ]
                    }
                    """;
            case BRIEFING -> """
                    {
                      "summary": "...",
                      "keyPoints": ["..."],
                      "openQuestions": ["..."]
                    }
                    """;
            case PODCAST_SCRIPT -> """
                    {
                      "segments": [
                        { "speaker": "Host A", "text": "..." },
                        { "speaker": "Host B", "text": "..." },
                        { "speaker": "Host A", "text": "..." },
                        { "speaker": "Host B", "text": "..." },
                        { "speaker": "Host A", "text": "..." },
                        { "speaker": "Host B", "text": "..." }
                      ]
                    }
                    """;
            case INFOGRAPHIC_OUTLINE -> """
                    {
                      "title": "...",
                      "sections": [
                        { "heading": "...", "points": ["short point", "short point"] },
                        { "heading": "...", "points": ["short point", "short point"] },
                        { "heading": "...", "points": ["short point", "short point"] },
                        { "heading": "...", "points": ["short point", "short point"] }
                      ]
                    }
                    """;
        };
    }

    private void validateArtifactData(
            StudioArtifactType type,
            JsonNode data
    ) {

        switch (type) {
            case FLASHCARDS -> validateFlashcards(data);
            case QUIZ -> validateQuiz(data);
            case INFOGRAPHIC_OUTLINE -> validateInfographic(data);
            case BRIEFING, PODCAST_SCRIPT -> {
            }
        }
    }

    private void validateFlashcards(
            JsonNode data
    ) {

        JsonNode cards =
                data.path("cards");

        if (!cards.isArray() || cards.size() != 6) {
            throw new RuntimeException(
                    "Studio flashcards must include exactly 6 cards"
            );
        }

        for (JsonNode card : cards) {
            if (card.path("front").asText().isBlank()
                    || card.path("back").asText().isBlank()) {
                throw new RuntimeException(
                        "Studio flashcards include an invalid card"
                );
            }
        }
    }

    private void validateQuiz(
            JsonNode data
    ) {

        JsonNode questions =
                data.path("questions");

        if (!questions.isArray() || questions.size() != 8) {
            throw new RuntimeException(
                    "Studio quiz must include exactly 8 questions"
            );
        }

        for (JsonNode question : questions) {
            if (question.path("question").asText().isBlank()
                    || !question.path("options").isArray()
                    || question.path("options").size() != 4
                    || question.path("answer").asText().isBlank()
                    || question.path("explanation").asText().isBlank()) {
                throw new RuntimeException(
                        "Studio quiz includes an invalid question"
                );
            }
        }
    }

    private void validateInfographic(
            JsonNode data
    ) {

        JsonNode sections =
                data.path("sections");

        if (data.path("title").asText().isBlank()
                || !sections.isArray()
                || sections.size() < 3) {
            throw new RuntimeException(
                    "Studio infographic must include a title and at least 3 sections"
            );
        }

        for (JsonNode section : sections) {
            if (section.path("heading").asText().isBlank()
                    || !section.path("points").isArray()
                    || section.path("points").size() < 2) {
                throw new RuntimeException(
                        "Studio infographic includes an invalid section"
                );
            }
        }
    }

    private String stripJsonFence(
            String response
    ) {

        String trimmed =
                response.trim();

        if (!trimmed.startsWith("```")) {
            return extractJsonObject(
                    trimmed
            );
        }

        return extractJsonObject(
                trimmed
                .replaceFirst("^```(?:json)?\\s*", "")
                .replaceFirst("\\s*```$", "")
                .trim()
        );
    }

    private String extractJsonObject(
            String response
    ) {

        int start =
                response.indexOf('{');

        int end =
                response.lastIndexOf('}');

        if (start < 0 || end <= start) {
            return response;
        }

        return response.substring(
                start,
                end + 1
        );
    }

    private String requiredText(
            JsonNode node,
            String field
    ) {

        JsonNode value =
                requiredNode(
                        node,
                        field
                );

        if (!value.isTextual() || value.asText().isBlank()) {
            throw new RuntimeException(
                    "Studio artifact response missing "
                            + field
            );
        }

        return value.asText();
    }

    private JsonNode requiredNode(
            JsonNode node,
            String field
    ) {

        JsonNode value =
                node.get(field);

        if (value == null || value.isNull()) {
            throw new RuntimeException(
                    "Studio artifact response missing "
                            + field
            );
        }

        return value;
    }

    private String toPrettyJson(
            Object value
    ) {

        try {
            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to serialize Studio artifact JSON",
                    e
            );
        }
    }

    private StudioArtifactResponse toResponse(
            StudioArtifact artifact
    ) {

        return new StudioArtifactResponse(
                artifact.getId().toString(),
                artifact.getNotebookId().toString(),
                artifact.getType(),
                artifact.getTitle(),
                artifact.getMarkdownContent(),
                artifact.getJsonContent(),
                parseSourceChunkIds(
                        artifact.getSourceChunkIds()
                ),
                artifact.getAudioFilePath() != null,
                artifact.getImageFilePath() != null,
                artifact.getCreatedAt()
        );
    }

    private void generateAudioIfPossible(
            StudioArtifact artifact
    ) {

        try {
            byte[] audio =
                    geminiTtsService.generatePodcastAudio(
                            artifact.getJsonContent()
                    );

            Files.createDirectories(
                    Paths.get(audioStorageDir)
            );

            Path audioPath =
                    Paths.get(
                            audioStorageDir,
                            artifact.getNotebookId()
                                    + "-"
                                    + UUID.randomUUID()
                                    + ".wav"
                    );

            Files.write(
                    audioPath,
                    audio
            );

            artifact.setAudioFilePath(
                    audioPath.toString()
            );

            artifact.setAudioMimeType(
                    "audio/wav"
            );
        } catch (Exception e) {
            artifact.setMarkdownContent(
                    artifact.getMarkdownContent()
                            + "\n\n> Podcast audio could not be generated right now. The script was saved and can be regenerated later."
            );
        }
    }

    private void deleteAudioFile(
            StudioArtifact artifact
    ) {

        if (artifact.getAudioFilePath() == null) {
            return;
        }

        try {
            Files.deleteIfExists(
                    Path.of(
                            artifact.getAudioFilePath()
                    )
            );
        } catch (Exception ignored) {
        }
    }

    private void generateInfographicImage(
            StudioArtifact artifact,
            JsonNode data
    ) {

        Path imagePath =
                Paths.get(
                        imageStorageDir,
                        artifact.getNotebookId()
                                + "-"
                                + UUID.randomUUID()
                                + ".png"
                );

        infographicImageRenderer.renderPng(
                data,
                imagePath
        );

        artifact.setImageFilePath(
                imagePath.toString()
        );

        artifact.setImageMimeType(
                "image/png"
        );
    }

    private void deleteImageFile(
            StudioArtifact artifact
    ) {

        if (artifact.getImageFilePath() == null) {
            return;
        }

        try {
            Files.deleteIfExists(
                    Path.of(
                            artifact.getImageFilePath()
                    )
            );
        } catch (Exception ignored) {
        }
    }

    private List<String> parseSourceChunkIds(
            String sourceChunkIds
    ) {

        if (sourceChunkIds == null || sourceChunkIds.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(
                    sourceChunkIds,
                    objectMapper.getTypeFactory()
                            .constructCollectionType(
                                    List.class,
                                    String.class
                            )
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse Studio source chunk ids",
                    e
            );
        }
    }
}
