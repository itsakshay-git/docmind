package com.docmind.docmind_api.document.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class YouTubeTranscriptIngestionService {

    private static final int TIMEOUT_MILLIS = 12000;

    private static final Pattern WATCH_PATTERN =
            Pattern.compile("[?&]v=([a-zA-Z0-9_-]{11})");

    private static final Pattern SHORT_PATTERN =
            Pattern.compile("youtu\\.be/([a-zA-Z0-9_-]{11})");

    private static final Pattern EMBED_PATTERN =
            Pattern.compile("/embed/([a-zA-Z0-9_-]{11})");

    private final ObjectMapper objectMapper;

    public YouTubeTranscript fetch(
            String url
    ) throws IOException {

        String videoId =
                extractVideoId(url);

        String text =
                fetchDirectTimedText(
                        videoId
                );

        if (text.isBlank()) {
            text = fetchCaptionTrackText(
                    videoId
            );
        }

        if (text.isBlank()) {
            throw new IllegalArgumentException(
                    "No English transcript is available for this YouTube video"
            );
        }

        return new YouTubeTranscript(
                "YouTube video " + videoId,
                "https://www.youtube.com/watch?v=" + videoId,
                text
        );
    }

    private String fetchDirectTimedText(
            String videoId
    ) throws IOException {

        String transcriptUrl =
                UriComponentsBuilder
                        .fromUriString("https://www.youtube.com/api/timedtext")
                        .queryParam("v", videoId)
                        .queryParam("lang", "en")
                        .build()
                        .toUriString();

        return fetchTranscriptXmlText(
                transcriptUrl
        );
    }

    private String fetchCaptionTrackText(
            String videoId
    ) throws IOException {

        String watchPage =
                Jsoup.connect(
                                "https://www.youtube.com/watch?v=" + videoId
                        )
                        .userAgent("Mozilla/5.0 DocMindBot/1.0")
                        .timeout(TIMEOUT_MILLIS)
                        .ignoreContentType(true)
                        .execute()
                        .body();

        String captionTracksJson =
                extractJsonArray(
                        watchPage,
                        "\"captionTracks\":"
                );

        if (captionTracksJson == null) {
            return "";
        }

        JsonNode tracks =
                objectMapper.readTree(
                        captionTracksJson
                );

        JsonNode selectedTrack =
                selectEnglishTrack(
                        tracks
                );

        if (selectedTrack == null || selectedTrack.get("baseUrl") == null) {
            return "";
        }

        return fetchTranscriptXmlText(
                selectedTrack.get("baseUrl")
                        .asText()
        );
    }

    private String fetchTranscriptXmlText(
            String transcriptUrl
    ) throws IOException {

        Document document =
                Jsoup.connect(transcriptUrl)
                        .userAgent("Mozilla/5.0 DocMindBot/1.0")
                        .timeout(TIMEOUT_MILLIS)
                        .ignoreContentType(true)
                        .get();

        return document.select("text")
                .eachText()
                .stream()
                .reduce(
                        "",
                        (left, right) -> left + " " + right
                )
                .trim();
    }

    private JsonNode selectEnglishTrack(
            JsonNode tracks
    ) {

        JsonNode fallback =
                null;

        for (JsonNode track : tracks) {
            String languageCode =
                    track.path("languageCode")
                            .asText("");

            String name =
                    track.path("name")
                            .path("simpleText")
                            .asText("");

            if (languageCode.equals("en")) {
                return track;
            }

            if (fallback == null
                    && (languageCode.startsWith("en")
                    || name.toLowerCase().contains("english"))) {
                fallback = track;
            }
        }

        return fallback;
    }

    private String extractJsonArray(
            String source,
            String marker
    ) {

        int markerIndex =
                source.indexOf(marker);

        if (markerIndex < 0) {
            return null;
        }

        int start =
                source.indexOf(
                        '[',
                        markerIndex + marker.length()
                );

        if (start < 0) {
            return null;
        }

        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int index = start; index < source.length(); index++) {
            char character =
                    source.charAt(index);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (character == '\\') {
                escaped = true;
                continue;
            }

            if (character == '"') {
                inString = !inString;
                continue;
            }

            if (inString) {
                continue;
            }

            if (character == '[') {
                depth++;
            }

            if (character == ']') {
                depth--;

                if (depth == 0) {
                    return source.substring(
                            start,
                            index + 1
                    );
                }
            }
        }

        return null;
    }

    String extractVideoId(
            String url
    ) {

        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException(
                    "YouTube URL is required"
            );
        }

        String trimmed =
                url.trim();

        try {
            URI.create(trimmed);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid YouTube URL"
            );
        }

        for (Pattern pattern : new Pattern[]{
                WATCH_PATTERN,
                SHORT_PATTERN,
                EMBED_PATTERN
        }) {
            Matcher matcher =
                    pattern.matcher(trimmed);

            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        throw new IllegalArgumentException(
                "Could not find a YouTube video id in the URL"
        );
    }

    public record YouTubeTranscript(
            String title,
            String url,
            String text
    ) {
    }
}
