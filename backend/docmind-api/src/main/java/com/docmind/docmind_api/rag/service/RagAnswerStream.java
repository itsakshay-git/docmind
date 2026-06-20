package com.docmind.docmind_api.rag.service;

import com.docmind.docmind_api.rag.dto.RagSource;
import reactor.core.publisher.Flux;

import java.util.List;

public record RagAnswerStream(
        Flux<String> tokens,
        List<RagSource> sources
) {
}