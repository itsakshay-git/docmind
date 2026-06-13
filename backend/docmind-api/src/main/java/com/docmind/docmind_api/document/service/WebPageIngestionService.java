package com.docmind.docmind_api.document.service;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class WebPageIngestionService {

    private static final int TIMEOUT_MILLIS = 12000;
    private static final int MAX_TEXT_LENGTH = 80_000;
    private static final int MIN_USEFUL_TEXT_LENGTH = 200;
    private static final Pattern JAVASCRIPT_STRING_PATTERN =
            Pattern.compile("\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"|'([^'\\\\]*(?:\\\\.[^'\\\\]*)*)'");

    public WebPageContent fetch(
            String url
    ) throws IOException {

        URI uri =
                URI.create(
                        normalizeUrl(url)
                );

        Document document =
                Jsoup.connect(
                                uri.toString()
                        )
                        .userAgent("DocMindBot/1.0")
                        .timeout(TIMEOUT_MILLIS)
                        .followRedirects(true)
                        .get();

        List<String> fallbackParts =
                extractFallbackParts(
                        document
                );

        List<String> scriptParts =
                extractSameOriginScriptText(
                        document,
                        uri
                );

        document.select("script, style, svg, nav, footer, header, form")
                .remove();

        String title =
                document.title()
                        .isBlank()
                        ? uri.toString()
                        : document.title();

        String text =
                document.body()
                        .text()
                        .trim();

        if (text.length() < MIN_USEFUL_TEXT_LENGTH) {
            text = combineText(
                    text,
                    fallbackParts,
                    scriptParts
            );
        }

        if (text.length() > MAX_TEXT_LENGTH) {
            text = text.substring(
                    0,
                    MAX_TEXT_LENGTH
            );
        }

        return new WebPageContent(
                title,
                uri.toString(),
                text
        );
    }

    private List<String> extractFallbackParts(
            Document document
    ) {

        List<String> parts =
                new ArrayList<>();

        addIfUseful(
                parts,
                document.title()
        );

        document.select("meta[name=description], meta[name=keywords], meta[property=og:title], meta[property=og:description], meta[name=twitter:title], meta[name=twitter:description]")
                .forEach(meta -> addIfUseful(
                        parts,
                        meta.attr("content")
                ));

        document.select("script[type=application/ld+json], script#__NEXT_DATA__, noscript")
                .forEach(element -> addIfUseful(
                        parts,
                        element.text()
                ));

        return parts;
    }

    private List<String> extractSameOriginScriptText(
            Document document,
            URI pageUri
    ) {

        Set<String> snippets =
                new LinkedHashSet<>();

        for (Element script : document.select("script[src]")) {
            if (snippets.size() >= 160) {
                break;
            }

            try {
                URI scriptUri =
                        pageUri.resolve(
                                script.attr("src")
                        );

                if (!pageUri.getHost()
                        .equalsIgnoreCase(
                                scriptUri.getHost()
                        )) {
                    continue;
                }

                String body =
                        Jsoup.connect(
                                        scriptUri.toString()
                                )
                                .userAgent("DocMindBot/1.0")
                                .timeout(TIMEOUT_MILLIS)
                                .ignoreContentType(true)
                                .execute()
                                .body();

                Matcher matcher =
                        JAVASCRIPT_STRING_PATTERN.matcher(
                                body
                        );

                while (matcher.find() && snippets.size() < 160) {
                    String value =
                            matcher.group(1) != null
                                    ? matcher.group(1)
                                    : matcher.group(2);

                    value = cleanJavaScriptString(
                            value
                    );

                    if (isUsefulSnippet(value)) {
                        snippets.add(value);
                    }
                }
            } catch (Exception ignored) {
                // Script extraction is a best-effort fallback for static SPAs.
            }
        }

        return new ArrayList<>(
                snippets
        );
    }

    private String combineText(
            String bodyText,
            List<String> fallbackParts,
            List<String> scriptParts
    ) {

        List<String> parts =
                new ArrayList<>();

        addIfUseful(
                parts,
                bodyText
        );

        fallbackParts.forEach(part -> addIfUseful(
                parts,
                part
        ));

        scriptParts.forEach(part -> addIfUseful(
                parts,
                part
        ));

        return String.join(
                "\n\n",
                parts
        ).trim();
    }

    private void addIfUseful(
            List<String> parts,
            String value
    ) {

        if (value == null) {
            return;
        }

        String cleaned =
                value.replaceAll("\\s+", " ")
                        .trim();

        if (!cleaned.isBlank()) {
            parts.add(cleaned);
        }
    }

    private String cleanJavaScriptString(
            String value
    ) {

        return value
                .replace("\\n", " ")
                .replace("\\t", " ")
                .replace("\\\"", "\"")
                .replace("\\'", "'")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean isUsefulSnippet(
            String value
    ) {

        if (value.length() < 24 || value.length() > 500) {
            return false;
        }

        if (!value.contains(" ")) {
            return false;
        }

        String lower =
                value.toLowerCase();

        return !lower.startsWith("http")
                && !lower.contains("function(")
                && !lower.contains("=>")
                && !lower.contains("classname")
                && !lower.contains("modulepreload")
                && !lower.contains("application/json");
    }

    private String normalizeUrl(
            String url
    ) {

        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException(
                    "URL is required"
            );
        }

        String trimmed =
                url.trim();

        if (!trimmed.startsWith("http://")
                && !trimmed.startsWith("https://")) {
            return "https://" + trimmed;
        }

        return trimmed;
    }

    public record WebPageContent(
            String title,
            String url,
            String text
    ) {
    }
}
