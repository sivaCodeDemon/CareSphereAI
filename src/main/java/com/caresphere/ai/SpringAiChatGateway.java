package com.caresphere.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * Spring AI backed implementation of {@link ChatGateway}. The {@link ChatClient.Builder}
 * bean is auto-configured by Spring AI when a chat model is available.
 */
@Component
public class SpringAiChatGateway implements ChatGateway {

    private final ChatClient chatClient;

    public SpringAiChatGateway(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public String chat(String systemPrompt, String userPrompt) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }
}
