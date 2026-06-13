package com.docmind.docmind_api.studio.dto;

import com.docmind.docmind_api.studio.enums.StudioArtifactType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class StudioArtifactResponse {

    private String id;

    private String notebookId;

    private StudioArtifactType type;

    private String title;

    private String markdownContent;

    private String jsonContent;

    private List<String> sourceChunkIds;

    private boolean audioAvailable;

    private boolean imageAvailable;

    private LocalDateTime createdAt;
}
