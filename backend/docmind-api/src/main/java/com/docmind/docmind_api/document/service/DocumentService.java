package com.docmind.docmind_api.document.service;

import com.docmind.docmind_api.ai.service.EmbeddingService;
import com.docmind.docmind_api.document.chunking.TextChunkingService;
import com.docmind.docmind_api.document.dto.AddYouTubeTranscriptRequest;
import com.docmind.docmind_api.document.dto.DocumentResponse;
import com.docmind.docmind_api.document.entity.Document;
import com.docmind.docmind_api.document.enums.DocumentSourceType;
import com.docmind.docmind_api.document.enums.DocumentStatus;
import com.docmind.docmind_api.document.parser.PdfParser;
import com.docmind.docmind_api.document.repository.DocumentRepository;
import com.docmind.docmind_api.notebook.entity.Notebook;
import com.docmind.docmind_api.notebook.repository.NotebookRepository;
import com.docmind.docmind_api.rag.entity.Chunk;
import com.docmind.docmind_api.rag.repository.ChunkRepository;
import com.docmind.docmind_api.rag.repository.EmbeddingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final PdfParser pdfParser;
    private final TextChunkingService textChunkingService;
    private final ChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;
    private final NotebookRepository notebookRepository;
    private final EmbeddingRepository embeddingRepository;
    private final WebPageIngestionService webPageIngestionService;
    private final YouTubeTranscriptIngestionService youTubeTranscriptIngestionService;

    @Transactional
    public String upload(
            UUID notebookId,
            MultipartFile file,
            String ownerEmail
    ) throws IOException {

        notebookRepository
                .findByIdAndOwnerEmail(
                        notebookId,
                        ownerEmail
                )
                .orElseThrow();

        String fileName =
                UUID.randomUUID()
                        + "_"
                        + file.getOriginalFilename();

        Path uploadPath =
                Paths.get(
                        "storage/documents",
                        fileName
                );

        Files.createDirectories(
                uploadPath.getParent()
        );

        Files.copy(
                file.getInputStream(),
                uploadPath,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
        );

        Document document = new Document();

        document.setNotebookId(notebookId);
        document.setFileName(file.getOriginalFilename());
        document.setFilePath(uploadPath.toString());
        document.setSourceType(DocumentSourceType.PDF);
        document.setStatus(DocumentStatus.UPLOADED);

        documentRepository.save(document);

        String content =
                pdfParser.extractText(
                        uploadPath.toString()
                );

        indexDocumentText(
                document,
                content
        );

        return "Uploaded";
    }

    @Transactional
    public DocumentResponse addWebUrl(
            UUID notebookId,
            String url,
            String ownerEmail
    ) {

        notebookRepository
                .findByIdAndOwnerEmail(
                        notebookId,
                        ownerEmail
                )
                .orElseThrow();

        Document document =
                createSourceDocument(
                        notebookId,
                        "Website",
                        DocumentSourceType.WEB_URL,
                        url
                );

        try {
            WebPageIngestionService.WebPageContent content =
                    webPageIngestionService.fetch(url);

            document.setFileName(
                    content.title()
            );
            document.setSourceUrl(
                    content.url()
            );

            indexDocumentText(
                    document,
                    content.text()
            );
        } catch (Exception e) {
            markFailed(
                    document,
                    e.getMessage()
            );
        }

        return toResponse(document);
    }

    @Transactional
    public DocumentResponse addYouTube(
            UUID notebookId,
            String url,
            String ownerEmail
    ) {

        notebookRepository
                .findByIdAndOwnerEmail(
                        notebookId,
                        ownerEmail
                )
                .orElseThrow();

        Document document =
                createSourceDocument(
                        notebookId,
                        "YouTube video",
                        DocumentSourceType.YOUTUBE,
                        url
                );

        try {
            YouTubeTranscriptIngestionService.YouTubeTranscript transcript =
                    youTubeTranscriptIngestionService.fetch(url);

            document.setFileName(
                    transcript.title()
            );
            document.setSourceUrl(
                    transcript.url()
            );

            indexDocumentText(
                    document,
                    transcript.text()
            );
        } catch (Exception e) {
            markFailed(
                    document,
                    e.getMessage()
            );
        }

        return toResponse(document);
    }

    @Transactional
    public DocumentResponse addYouTubeTranscript(
            UUID notebookId,
            AddYouTubeTranscriptRequest request,
            String ownerEmail
    ) {

        notebookRepository
                .findByIdAndOwnerEmail(
                        notebookId,
                        ownerEmail
                )
                .orElseThrow();

        String transcript =
                request.getTranscript();

        if (transcript == null || transcript.isBlank()) {
            throw new IllegalArgumentException(
                    "Transcript text is required"
            );
        }

        String title =
                request.getTitle() == null
                        || request.getTitle().isBlank()
                        ? "YouTube transcript"
                        : request.getTitle().trim();

        Document document =
                createSourceDocument(
                        notebookId,
                        title,
                        DocumentSourceType.YOUTUBE_TRANSCRIPT,
                        request.getUrl()
                );

        indexDocumentText(
                document,
                transcript.trim()
        );

        return toResponse(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getMyDocuments(
            String ownerEmail
    ) {

        List<UUID> notebookIds =
                notebookRepository.findByOwnerEmail(ownerEmail)
                        .stream()
                        .map(Notebook::getId)
                        .toList();

        if (notebookIds.isEmpty()) {
            return List.of();
        }

        return documentRepository
                .findByNotebookIdIn(notebookIds)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getNotebookDocuments(
            UUID notebookId,
            String ownerEmail
    ) {

        notebookRepository
                .findByIdAndOwnerEmail(
                        notebookId,
                        ownerEmail
                )
                .orElseThrow();

        return documentRepository
                .findByNotebookId(notebookId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countDocumentsForNotebook(
            UUID notebookId
    ) {

        return documentRepository.countByNotebookId(
                notebookId
        );
    }

    @Transactional
    public void deleteDocument(
            UUID documentId,
            String ownerEmail
    ) throws IOException {

        Document document =
                documentRepository
                        .findById(documentId)
                        .orElseThrow();

        notebookRepository
                .findByIdAndOwnerEmail(
                        document.getNotebookId(),
                        ownerEmail
                )
                .orElseThrow();

        deleteDocumentData(document);
    }

    @Transactional
    public void deleteDocumentsForNotebook(
            UUID notebookId
    ) {

        documentRepository
                .findByNotebookId(notebookId)
                .forEach(document -> {
                    try {
                        deleteDocumentData(document);
                    } catch (IOException e) {
                        throw new RuntimeException(
                                "Failed to delete document file",
                                e
                        );
                    }
                });
    }

    private void deleteDocumentData(
            Document document
    ) throws IOException {

        List<Chunk> chunks =
                chunkRepository.findByDocumentId(
                        document.getId()
                );

        List<UUID> chunkIds =
                chunks.stream()
                        .map(Chunk::getId)
                        .toList();

        if (!chunkIds.isEmpty()) {
            embeddingRepository.deleteByChunkIdIn(
                    chunkIds
            );
        }

        chunkRepository.deleteByDocumentId(
                document.getId()
        );

        documentRepository.delete(document);

        if (document.getFilePath() != null) {
            Files.deleteIfExists(
                    Paths.get(
                            document.getFilePath()
                    )
            );
        }
    }

    private DocumentResponse toResponse(
            Document document
    ) {

        return new DocumentResponse(
                document.getId().toString(),
                document.getNotebookId().toString(),
                document.getFileName(),
                document.getSourceType(),
                document.getSourceUrl(),
                document.getStatus(),
                document.getFailureReason(),
                document.getCreatedAt()
        );
    }

    private Document createSourceDocument(
            UUID notebookId,
            String fileName,
            DocumentSourceType sourceType,
            String sourceUrl
    ) {

        Document document =
                new Document();

        document.setNotebookId(notebookId);
        document.setFileName(fileName);
        document.setSourceType(sourceType);
        document.setSourceUrl(sourceUrl);
        document.setStatus(DocumentStatus.PROCESSING);

        return documentRepository.save(document);
    }

    private void indexDocumentText(
            Document document,
            String content
    ) {

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "No text content could be extracted from this source"
            );
        }

        document.setContent(content);
        document.setStatus(DocumentStatus.PROCESSED);
        document.setFailureReason(null);

        documentRepository.save(document);

        List<String> chunks =
                textChunkingService.chunk(
                        content,
                        1000
                );

        List<Chunk> chunkEntities = new ArrayList<>();

        int index = 0;

        for (String chunkContent : chunks) {

            Chunk chunk = new Chunk();

            chunk.setDocumentId(document.getId());
            chunk.setContent(chunkContent);
            chunk.setChunkIndex(index++);

            chunkEntities.add(chunk);
        }

        List<Chunk> savedChunks =
                chunkRepository.saveAll(chunkEntities);

        embeddingService.generateAndSaveEmbeddings(
                savedChunks
        );
    }

    private void markFailed(
            Document document,
            String reason
    ) {

        document.setStatus(DocumentStatus.FAILED);
        document.setFailureReason(
                reason == null || reason.isBlank()
                        ? "Source ingestion failed"
                        : reason
        );

        documentRepository.save(document);
    }
}
