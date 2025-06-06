package org.acme;

import dev.langchain4j.model.chat.ChatModel;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import jakarta.inject.Inject;
import java.util.List;

@WebSocket(path = "/chat")
public class WebSocketChatBot {

    @Inject
    ChatModel chatLanguageModel;

    @Inject
    SentimentAnalysis sentimentAnalysis;

    @OnTextMessage
    public String onMessage(String message) {
        Evaluation evaluation = sentimentAnalysis.triage(
            List.of(Evaluation.values()),
            message);
        return evaluation.name();
    }

}
