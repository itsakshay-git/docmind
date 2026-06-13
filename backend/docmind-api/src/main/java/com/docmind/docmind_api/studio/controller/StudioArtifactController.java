package com.docmind.docmind_api.studio.controller;

import com.docmind.docmind_api.studio.dto.GenerateStudioArtifactRequest;
import com.docmind.docmind_api.studio.dto.StudioArtifactResponse;
import com.docmind.docmind_api.studio.entity.StudioArtifact;
import com.docmind.docmind_api.studio.service.StudioArtifactService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/studio")
@RequiredArgsConstructor
public class StudioArtifactController {

    private final StudioArtifactService studioArtifactService;

    @GetMapping("/notebooks/{notebookId}/artifacts")
    public List<StudioArtifactResponse> listArtifacts(
            @PathVariable UUID notebookId,
            Authentication authentication
    ) {

        return studioArtifactService.listArtifacts(
                notebookId,
                authentication.getName()
        );
    }

    @GetMapping("/artifacts/{artifactId}")
    public StudioArtifactResponse getArtifact(
            @PathVariable UUID artifactId,
            Authentication authentication
    ) {

        return studioArtifactService.getArtifact(
                artifactId,
                authentication.getName()
        );
    }

    @PostMapping("/notebooks/{notebookId}/artifacts")
    public StudioArtifactResponse generateArtifact(
            @PathVariable UUID notebookId,
            @RequestBody GenerateStudioArtifactRequest request,
            Authentication authentication
    ) {

        return studioArtifactService.generateArtifact(
                notebookId,
                request,
                authentication.getName()
        );
    }

    @DeleteMapping("/artifacts/{artifactId}")
    public void deleteArtifact(
            @PathVariable UUID artifactId,
            Authentication authentication
    ) {

        studioArtifactService.deleteArtifact(
                artifactId,
                authentication.getName()
        );
    }

    @GetMapping("/artifacts/{artifactId}/download")
    public ResponseEntity<?> downloadArtifact(
            @PathVariable UUID artifactId,
            @RequestParam(defaultValue = "markdown") String format,
            Authentication authentication
    ) {

        StudioArtifact artifact =
                studioArtifactService.getOwnedArtifact(
                        artifactId,
                        authentication.getName()
                );

        String normalized =
                format.toLowerCase(
                        Locale.ROOT
                );

        if ("json".equals(normalized)) {
            return download(
                    artifact.getJsonContent(),
                    artifact.getTitle(),
                    "json",
                    MediaType.APPLICATION_JSON
            );
        }

        if ("audio".equals(normalized)) {
            if (artifact.getAudioFilePath() == null) {
                throw new IllegalArgumentException(
                        "Audio is not available for this artifact"
                );
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(
                            artifact.getAudioMimeType() == null
                                    ? "audio/wav"
                                    : artifact.getAudioMimeType()
                    ))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment()
                                    .filename(
                                            filename(
                                                    artifact.getTitle(),
                                                    "wav"
                                            )
                                    )
                                    .build()
                                    .toString()
                    )
                    .body(
                            new FileSystemResource(
                                    artifact.getAudioFilePath()
                            )
                    );
        }

        if ("png".equals(normalized)) {
            return imageDownload(
                    artifact,
                    "png",
                    MediaType.IMAGE_PNG
            );
        }

        if ("jpg".equals(normalized) || "jpeg".equals(normalized)) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment()
                                    .filename(
                                            filename(
                                                    artifact.getTitle(),
                                                    "jpg"
                                            )
                                    )
                                    .build()
                                    .toString()
                    )
                    .body(
                            toJpegBytes(
                                    artifact
                            )
                    );
        }

        if (!"markdown".equals(normalized)) {
            throw new IllegalArgumentException(
                    "Unsupported download format"
            );
        }

        return download(
                artifact.getMarkdownContent(),
                artifact.getTitle(),
                "md",
                MediaType.valueOf("text/markdown")
        );
    }

    private ResponseEntity<String> download(
            String body,
            String title,
            String extension,
            MediaType mediaType
    ) {

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(filename(
                                        title,
                                        extension
                                ))
                                .build()
                                .toString()
                )
                .body(body);
    }

    @GetMapping("/artifacts/{artifactId}/audio")
    public ResponseEntity<Resource> getArtifactAudio(
            @PathVariable UUID artifactId,
            Authentication authentication
    ) {

        StudioArtifact artifact =
                studioArtifactService.getOwnedArtifact(
                        artifactId,
                        authentication.getName()
                );

        if (artifact.getAudioFilePath() == null) {
            throw new IllegalArgumentException(
                    "Audio is not available for this artifact"
            );
        }

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(
                        artifact.getAudioMimeType() == null
                                ? "audio/wav"
                                : artifact.getAudioMimeType()
                ))
                .body(
                        new FileSystemResource(
                                artifact.getAudioFilePath()
                        )
                );
    }

    @GetMapping("/artifacts/{artifactId}/image")
    public ResponseEntity<Resource> getArtifactImage(
            @PathVariable UUID artifactId,
            Authentication authentication
    ) {

        StudioArtifact artifact =
                studioArtifactService.getOwnedArtifact(
                        artifactId,
                        authentication.getName()
                );

        if (artifact.getImageFilePath() == null) {
            throw new IllegalArgumentException(
                    "Image is not available for this artifact"
            );
        }

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(
                        artifact.getImageMimeType() == null
                                ? "image/png"
                                : artifact.getImageMimeType()
                ))
                .body(
                        new FileSystemResource(
                                artifact.getImageFilePath()
                        )
                );
    }

    private ResponseEntity<Resource> imageDownload(
            StudioArtifact artifact,
            String extension,
            MediaType mediaType
    ) {

        if (artifact.getImageFilePath() == null) {
            throw new IllegalArgumentException(
                    "Image is not available for this artifact"
            );
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(
                                        filename(
                                                artifact.getTitle(),
                                                extension
                                        )
                                )
                                .build()
                                .toString()
                )
                .body(
                        new FileSystemResource(
                                artifact.getImageFilePath()
                        )
                );
    }

    private byte[] toJpegBytes(
            StudioArtifact artifact
    ) {

        if (artifact.getImageFilePath() == null) {
            throw new IllegalArgumentException(
                    "Image is not available for this artifact"
            );
        }

        try {
            BufferedImage png =
                    ImageIO.read(
                            new FileSystemResource(
                                    artifact.getImageFilePath()
                            ).getFile()
                    );

            BufferedImage jpg =
                    new BufferedImage(
                            png.getWidth(),
                            png.getHeight(),
                            BufferedImage.TYPE_INT_RGB
                    );

            Graphics2D graphics =
                    jpg.createGraphics();

            graphics.setColor(
                    Color.WHITE
            );
            graphics.fillRect(
                    0,
                    0,
                    jpg.getWidth(),
                    jpg.getHeight()
            );
            graphics.drawImage(
                    png,
                    0,
                    0,
                    null
            );
            graphics.dispose();

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            ImageIO.write(
                    jpg,
                    "jpg",
                    output
            );

            return output.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to convert infographic to JPG",
                    e
            );
        }
    }

    private String filename(
            String title,
            String extension
    ) {

        String base =
                title.toLowerCase()
                        .replaceAll("[^a-z0-9]+", "-")
                        .replaceAll("(^-|-$)", "");

        if (base.isBlank()) {
            base = "studio-artifact";
        }

        return base
                + "."
                + extension;
    }
}
