package com.docmind.docmind_api.document.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YouTubeTranscriptIngestionServiceTest {

    private final YouTubeTranscriptIngestionService service =
            new YouTubeTranscriptIngestionService(
                    new ObjectMapper()
            );

    @Test
    void extractsVideoIdFromSupportedYouTubeUrls() {

        assertThat(service.extractVideoId(
                "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        )).isEqualTo("dQw4w9WgXcQ");

        assertThat(service.extractVideoId(
                "https://youtu.be/dQw4w9WgXcQ"
        )).isEqualTo("dQw4w9WgXcQ");

        assertThat(service.extractVideoId(
                "https://www.youtube.com/embed/dQw4w9WgXcQ"
        )).isEqualTo("dQw4w9WgXcQ");
    }

    @Test
    void rejectsUrlsWithoutVideoId() {

        assertThatThrownBy(() -> service.extractVideoId(
                "https://www.youtube.com/"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Could not find");
    }
}
