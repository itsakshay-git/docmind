package com.docmind.docmind_api.studio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiTtsService {

    private static final int SAMPLE_RATE = 24000;
    private static final short CHANNELS = 1;
    private static final short BITS_PER_SAMPLE = 16;

    private final ObjectMapper objectMapper;

    @Value("${spring.ai.google.genai.api-key}")
    private String apiKey;

    @Value("${docmind.studio.tts-model}")
    private String model;

    @Value("${docmind.studio.tts-host-a-voice}")
    private String hostAVoice;

    @Value("${docmind.studio.tts-host-b-voice}")
    private String hostBVoice;

    public byte[] generatePodcastAudio(
            String scriptPayload
    ) {

        try {
            String dialogue =
                    toPodcastDialogue(
                            scriptPayload
                    );

            String body =
                    objectMapper.writeValueAsString(
                            requestBody(dialogue)
                    );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            "https://generativelanguage.googleapis.com/v1beta/models/"
                                                    + model
                                                    + ":generateContent"
                                    )
                            )
                            .header(
                                    "x-goog-api-key",
                                    apiKey
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            body
                                    )
                            )
                            .build();

            HttpResponse<String> response =
                    HttpClient.newHttpClient()
                            .send(
                                    request,
                                    HttpResponse.BodyHandlers.ofString()
                            );

            if (response.statusCode() >= 400) {
                throw new RuntimeException(
                        "Gemini TTS failed with status "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            JsonNode root =
                    objectMapper.readTree(
                            response.body()
                    );

            String audioData =
                    root.path("candidates")
                            .path(0)
                            .path("content")
                            .path("parts")
                            .path(0)
                            .path("inlineData")
                            .path("data")
                            .asText();

            if (audioData == null || audioData.isBlank()) {
                throw new RuntimeException(
                        "Gemini TTS response did not include audio data"
                );
            }

            byte[] pcm =
                    Base64.getDecoder()
                            .decode(audioData);

            return toWav(
                    pcm
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to generate podcast audio",
                    e
            );
        }
    }

    private Map<String, Object> requestBody(
            String dialogue
    ) {

        return Map.of(
                "contents",
                new Object[]{
                        Map.of(
                                "parts",
                                new Object[]{
                                        Map.of(
                                                "text",
                                                """
                                                        Generate natural two-host educational podcast audio from this dialogue.
                                                        Use Host A as a male host and Host B as a female host.
                                                        Make the two voices clearly different.
                                                        Do not read speaker labels aloud.
                                                        Keep the delivery conversational, with short pauses between speaker turns.

                                                        %s
                                                        """.formatted(dialogue)
                                        )
                                }
                        )
                },
                "generationConfig",
                Map.of(
                        "responseModalities",
                        new String[]{"AUDIO"},
                        "speechConfig",
                        Map.of(
                                "multiSpeakerVoiceConfig",
                                Map.of(
                                        "speakerVoiceConfigs",
                                        new Object[]{
                                                Map.of(
                                                        "speaker",
                                                        "Host A",
                                                        "voiceConfig",
                                                        Map.of(
                                                                "prebuiltVoiceConfig",
                                                                Map.of(
                                                                        "voiceName",
                                                                        hostAVoice
                                                                )
                                                        )
                                                ),
                                                Map.of(
                                                        "speaker",
                                                        "Host B",
                                                        "voiceConfig",
                                                        Map.of(
                                                                "prebuiltVoiceConfig",
                                                                Map.of(
                                                                        "voiceName",
                                                                        hostBVoice
                                                                )
                                                        )
                                                )
                                        }
                                )
                        )
                )
        );
    }

    private String toPodcastDialogue(
            String scriptPayload
    ) {

        try {
            JsonNode root =
                    objectMapper.readTree(
                            scriptPayload
                    );

            JsonNode segments =
                    root.path("segments");

            if (!segments.isArray() || segments.isEmpty()) {
                return scriptPayload;
            }

            StringBuilder dialogue =
                    new StringBuilder();

            for (JsonNode segment : segments) {
                String speaker =
                        segment.path("speaker")
                                .asText("Host A")
                                .trim();

                String text =
                        segment.path("text")
                                .asText()
                                .trim();

                if (text.isBlank()) {
                    continue;
                }

                if (!"Host B".equalsIgnoreCase(speaker)) {
                    speaker = "Host A";
                } else {
                    speaker = "Host B";
                }

                dialogue.append(speaker)
                        .append(": ")
                        .append(text)
                        .append("\n\n");
            }

            if (dialogue.isEmpty()) {
                return scriptPayload;
            }

            return dialogue.toString()
                    .trim();
        } catch (Exception ignored) {
            return scriptPayload;
        }
    }

    private byte[] toWav(
            byte[] pcm
    ) throws Exception {

        int byteRate =
                SAMPLE_RATE
                        * CHANNELS
                        * BITS_PER_SAMPLE
                        / 8;

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        output.write("RIFF".getBytes());
        writeInt(output, 36 + pcm.length);
        output.write("WAVE".getBytes());
        output.write("fmt ".getBytes());
        writeInt(output, 16);
        writeShort(output, (short) 1);
        writeShort(output, CHANNELS);
        writeInt(output, SAMPLE_RATE);
        writeInt(output, byteRate);
        writeShort(output, (short) (CHANNELS * BITS_PER_SAMPLE / 8));
        writeShort(output, BITS_PER_SAMPLE);
        output.write("data".getBytes());
        writeInt(output, pcm.length);
        output.write(pcm);

        return output.toByteArray();
    }

    private void writeInt(
            ByteArrayOutputStream output,
            int value
    ) throws Exception {

        output.write(
                ByteBuffer.allocate(4)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putInt(value)
                        .array()
        );
    }

    private void writeShort(
            ByteArrayOutputStream output,
            short value
    ) throws Exception {

        output.write(
                ByteBuffer.allocate(2)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putShort(value)
                        .array()
        );
    }
}
