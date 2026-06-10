package com.docmind.docmind_api.ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AiTestController {

    private final ChatModel chatModel;

    @GetMapping("/test")
    public String test() {
        return "AI Connected";
    }
}