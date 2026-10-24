package com.caresphere.ai.dto;

import java.util.List;

public record AnswerResponse(String answer, boolean evidenceSufficient, List<Citation> citations) {

    public static AnswerResponse insufficient(List<Citation> citations) {
        return new AnswerResponse(
                "No sufficiently relevant passages were found in the indexed documents to answer this question.",
                false,
                citations);
    }
}
