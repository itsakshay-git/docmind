package com.docmind.docmind_api.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RagAskResponse {

    private String answer;

    private List<RagSource> sources;
}
