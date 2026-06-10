package com.docmind.docmind_api.document.service;

import com.docmind.docmind_api.document.chunking.TextChunkingService;
import com.docmind.docmind_api.document.entity.Document;
import com.docmind.docmind_api.document.enums.DocumentStatus;
import com.docmind.docmind_api.document.parser.PdfParser;
import com.docmind.docmind_api.document.repository.DocumentRepository;
import com.docmind.docmind_api.rag.entity.Chunk;
import com.docmind.docmind_api.rag.repository.ChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final PdfParser pdfParser;

    private final TextChunkingService textChunkingService;
    private final ChunkRepository chunkRepository;

    public String upload(
            UUID notebookId,
            MultipartFile file
    ) throws IOException {

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

//        Files.copy(
//                file.getInputStream(),
//                uploadPath
//        );

        Files.copy(
                file.getInputStream(),
                uploadPath,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
        );

        Document document =
                new Document();

        document.setNotebookId(notebookId);
        document.setFileName(
                file.getOriginalFilename()
        );
        document.setFilePath(
                uploadPath.toString()
        );
        document.setStatus(
                DocumentStatus.UPLOADED
        );

        documentRepository.save(document);

        String content =
                pdfParser.extractText(
                        uploadPath.toString()
                );

        document.setContent(content);

        document.setStatus(
                DocumentStatus.PROCESSED
        );

        documentRepository.save(document);

        List<String> chunks =
                textChunkingService.chunk(
                        content,
                        1000
                );

        int index = 0;

        for (String chunkContent : chunks) {

            Chunk chunk = new Chunk();

            chunk.setDocumentId(
                    document.getId()
            );

            chunk.setContent(
                    chunkContent
            );

            chunk.setChunkIndex(
                    index++
            );

            chunkRepository.save(
                    chunk
            );
        }

        return "Uploaded";
    }
}
