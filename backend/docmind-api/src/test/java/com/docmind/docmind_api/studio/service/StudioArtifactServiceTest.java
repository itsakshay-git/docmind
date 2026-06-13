package com.docmind.docmind_api.studio.service;

import com.docmind.docmind_api.notebook.entity.Notebook;
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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudioArtifactServiceTest {

    private final NotebookRepository notebookRepository =
            mock(NotebookRepository.class);

    private final RagSearchService ragSearchService =
            mock(RagSearchService.class);

    private final StudioArtifactRepository studioArtifactRepository =
            mock(StudioArtifactRepository.class);

    private final ChatModel chatModel =
            mock(ChatModel.class);

    private final GeminiTtsService geminiTtsService =
            mock(GeminiTtsService.class);

    private final InfographicImageRenderer infographicImageRenderer =
            mock(InfographicImageRenderer.class);

    private final StudioArtifactService studioArtifactService =
            new StudioArtifactService(
                    notebookRepository,
                    ragSearchService,
                    studioArtifactRepository,
                    chatModel,
                    geminiTtsService,
                    infographicImageRenderer,
                    new ObjectMapper()
            );

    @Test
    void throwsClearErrorWhenNotebookHasNoContext() {
        UUID notebookId =
                UUID.randomUUID();

        when(
                notebookRepository.findByIdAndOwnerEmail(
                        notebookId,
                        "user@example.com"
                )
        ).thenReturn(
                Optional.of(new Notebook())
        );

        when(
                ragSearchService.search(
                        any(UUID.class),
                        any(SemanticSearchRequest.class),
                        any(String.class)
                )
        ).thenReturn(
                List.of()
        );

        assertThatThrownBy(() -> studioArtifactService.generateArtifact(
                notebookId,
                request(StudioArtifactType.FLASHCARDS),
                "user@example.com"
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(
                        "Add sources before generating Studio artifacts."
                );

        verify(chatModel, never())
                .call(
                        any(String.class)
                );
    }

    @Test
    void generatesAndSavesArtifactFromRetrievedContext() {
        UUID notebookId =
                UUID.randomUUID();

        when(
                notebookRepository.findByIdAndOwnerEmail(
                        notebookId,
                        "user@example.com"
                )
        ).thenReturn(
                Optional.of(new Notebook())
        );

        when(
                ragSearchService.search(
                        any(UUID.class),
                        any(SemanticSearchRequest.class),
                        any(String.class)
                )
        ).thenReturn(
                List.of(
                        new SemanticSearchResult(
                                "chunk-1",
                                "document-1",
                                "Java has primitive data types.",
                                0.91
                        )
                )
        );

        when(
                chatModel.call(
                        any(String.class)
                )
        ).thenReturn(
                """
                        {
                          "title": "Java Flashcards",
                          "markdownContent": "## Java Flashcards\\n- What is int?",
                          "data": {
                            "cards": [
                              {
                                "front": "What is int?",
                                "back": "A primitive integer type.",
                                "difficulty": "basic"
                              },
                              {
                                "front": "What is byte?",
                                "back": "An 8-bit signed integer type.",
                                "difficulty": "basic"
                              },
                              {
                                "front": "What is short?",
                                "back": "A 16-bit signed integer type.",
                                "difficulty": "basic"
                              },
                              {
                                "front": "What is long?",
                                "back": "A 64-bit signed integer type.",
                                "difficulty": "medium"
                              },
                              {
                                "front": "What is float?",
                                "back": "A single-precision floating-point type.",
                                "difficulty": "medium"
                              },
                              {
                                "front": "What is double?",
                                "back": "A double-precision floating-point type.",
                                "difficulty": "advanced"
                              }
                            ]
                          }
                        }
                        """
        );

        when(
                studioArtifactRepository.save(
                        any(StudioArtifact.class)
                )
        ).thenAnswer(invocation -> {
            StudioArtifact artifact =
                    invocation.getArgument(0);

            artifact.setId(
                    UUID.randomUUID()
            );

            artifact.prePersist();

            return artifact;
        });

        StudioArtifactResponse response =
                studioArtifactService.generateArtifact(
                        notebookId,
                        request(StudioArtifactType.FLASHCARDS),
                        "user@example.com"
                );

        assertThat(response.getTitle())
                .isEqualTo("Java Flashcards");

        assertThat(response.getMarkdownContent())
                .contains("Java Flashcards");

        assertThat(response.getJsonContent())
                .contains("\"cards\"");

        assertThat(response.getSourceChunkIds())
                .containsExactly("chunk-1");

        ArgumentCaptor<SemanticSearchRequest> searchCaptor =
                ArgumentCaptor.forClass(
                        SemanticSearchRequest.class
                );

        verify(ragSearchService)
                .search(
                        any(UUID.class),
                        searchCaptor.capture(),
                        any(String.class)
                );

        assertThat(searchCaptor.getValue().getTopK())
                .isEqualTo(12);
    }

    @Test
    void rejectsInvalidFlashcardShape() {
        UUID notebookId =
                UUID.randomUUID();

        stubOwnedNotebookWithContext(notebookId);

        when(
                chatModel.call(
                        any(String.class)
                )
        ).thenReturn(
                """
                        {
                          "title": "Thin Flashcards",
                          "markdownContent": "Too thin",
                          "data": {
                            "cards": [
                              {
                                "front": "Only one?",
                                "back": "Invalid.",
                                "difficulty": "basic"
                              }
                            ]
                          }
                        }
                        """
        );

        assertThatThrownBy(() -> studioArtifactService.generateArtifact(
                notebookId,
                request(StudioArtifactType.FLASHCARDS),
                "user@example.com"
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(
                        "Studio flashcards must include exactly 6 cards"
                );
    }

    @Test
    void infographicGenerationStoresImageMetadata() {
        UUID notebookId =
                UUID.randomUUID();

        ReflectionTestUtils.setField(
                studioArtifactService,
                "imageStorageDir",
                "storage/studio-images"
        );

        stubOwnedNotebookWithContext(notebookId);

        when(
                chatModel.call(
                        any(String.class)
                )
        ).thenReturn(
                """
                        {
                          "title": "Java Types Infographic",
                          "markdownContent": "## Java Types",
                          "data": {
                            "title": "Java Types",
                            "sections": [
                              { "heading": "Primitive types", "points": ["Java has eight primitives.", "They store simple values."] },
                              { "heading": "Integers", "points": ["byte, short, int, and long are integral.", "Each type has a fixed size."] },
                              { "heading": "Floating point", "points": ["float and double store decimals.", "double has more precision."] }
                            ]
                          }
                        }
                        """
        );

        when(
                studioArtifactRepository.save(
                        any(StudioArtifact.class)
                )
        ).thenAnswer(invocation -> {
            StudioArtifact artifact =
                    invocation.getArgument(0);

            artifact.setId(
                    UUID.randomUUID()
            );

            artifact.prePersist();

            return artifact;
        });

        StudioArtifactResponse response =
                studioArtifactService.generateArtifact(
                        notebookId,
                        request(StudioArtifactType.INFOGRAPHIC_OUTLINE),
                        "user@example.com"
                );

        assertThat(response.isImageAvailable())
                .isTrue();

        ArgumentCaptor<Path> pathCaptor =
                ArgumentCaptor.forClass(
                        Path.class
                );

        verify(infographicImageRenderer)
                .renderPng(
                        any(JsonNode.class),
                        pathCaptor.capture()
                );

        assertThat(pathCaptor.getValue().toString())
                .contains("storage")
                .contains("studio-images")
                .endsWith(".png");
    }

    private void stubOwnedNotebookWithContext(
            UUID notebookId
    ) {

        when(
                notebookRepository.findByIdAndOwnerEmail(
                        notebookId,
                        "user@example.com"
                )
        ).thenReturn(
                Optional.of(new Notebook())
        );

        when(
                ragSearchService.search(
                        any(UUID.class),
                        any(SemanticSearchRequest.class),
                        any(String.class)
                )
        ).thenReturn(
                List.of(
                        new SemanticSearchResult(
                                "chunk-1",
                                "document-1",
                                "Java has primitive data types.",
                                0.91
                        )
                )
        );
    }

    private static GenerateStudioArtifactRequest request(
            StudioArtifactType type
    ) {

        GenerateStudioArtifactRequest request =
                new GenerateStudioArtifactRequest();

        request.setType(type);

        return request;
    }
}
