package com.docmind.docmind_api.document.chunking;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkingService {

    public List<String> chunk(
            String text,
            int chunkSize
    ) {

        List<String> chunks =
                new ArrayList<>();

        int start = 0;

        while (start < text.length()) {

            int end =
                    Math.min(
                            start + chunkSize,
                            text.length()
                    );

            chunks.add(
                    text.substring(start, end)
            );

            start = end;
        }

        return chunks;
    }
}