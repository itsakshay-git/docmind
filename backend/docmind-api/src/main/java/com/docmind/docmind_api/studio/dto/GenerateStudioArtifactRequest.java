package com.docmind.docmind_api.studio.dto;

import com.docmind.docmind_api.studio.enums.StudioArtifactType;
import lombok.Data;

@Data
public class GenerateStudioArtifactRequest {

    private StudioArtifactType type;

    private String instruction;
}
