package com.caresphere.ai;

/**
 * Thin abstraction over the LLM call so the RAG orchestration can be unit-tested without a
 * real chat client, and so the OpenAI dependency can be swapped for another provider.
 */
public interface ChatGateway {

    /**
     * Sends a system instruction and a user prompt to the model and returns the text answer.
     */
    String chat(String systemPrompt, String userPrompt);
}
