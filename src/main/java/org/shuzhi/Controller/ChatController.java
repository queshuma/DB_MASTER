package org.shuzhi.Controller;

import cn.dev33.satoken.stp.StpUtil;
import org.shuzhi.Service.DatabaseMetadataService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    public static final String COMPLETE = "[complete]";
    private final String DEFAULT_PROMPT = """
        你现在是一个RAG知识增强助手 
        """;



    private ChatClient chatClient;

    private VectorStore vectorStore;

    @Autowired
    private DatabaseMetadataService databaseMetadataService;

    public ChatController(ChatClient.Builder client, ChatMemory chatMemory, VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.chatClient = client.defaultSystem(DEFAULT_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    @GetMapping(value = "/ai/generateStreamAsString", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateStreamAsString(@RequestParam(value = "message", defaultValue = "你是谁，你能干什么！简短的描述") String message) {
        Flux<String> result = chatClient.prompt()
                .user(message)
                // 注入系统参数
                .system(spec -> spec.param("current_date", LocalDate.now()))
                // 添加问答适配器
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                // 构建响应流
                .stream()
                .content();

        return result.concatWith(Flux.just(COMPLETE));
    }
}
