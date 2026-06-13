package com.docmind.docmind_api.document.dto;

import lombok.Data;

@Data
public class AddYouTubeTranscriptRequest {

    private String url;

    private String title;

    private String transcript;
}
