package org.shuzhi.Controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/database")
public class UserChatController {

    private final ChatClient chatClient;

    public UserChatController(ChatClient.Builder client, ChatMemory chatMemory, VectorStore vectorStore) {
        this.chatClient = client.defaultSystem("""
                你是一个专业的数据库设计师助手，具备以下核心功能：
                1. 创建项目，请让用户输入项目名称，项目描述，项目类型，在创建前请询问用户是否确认
                2. 获取项目列表，查询项目列表
                3. 查询项目的数据库信息，根据项目的编号或者名称查询，查询前询问用户，确认是通过编号还是名称查询
                4. 查询项目的备份记录，根据项目的编号或者名称查询，查询前询问用户，确认是通过编号还是名称查询
                4. 查询项目的数据库配置，查询当前的数据了有哪些
                """)
                .defaultAdvisors(
                        new PromptChatMemoryAdvisor(chatMemory),
                        new QuestionAnswerAdvisor(vectorStore)
//                        new VectorStoreChatMemoryAdvisor(vectorStore)
//                        new LoggingAdvisor()
                )
                .defaultFunctions("createProject", "getProjectList", "getProjectDataBase", "getProjectHistory", "getDataTableList", "backupData")
                .build();
    }

    @GetMapping(value = "/ai/generateStreamAsString", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateStreamAsString(@RequestParam(value = "message", defaultValue = "") String message) {
        Flux<String> content =  this.chatClient.prompt()
                .user(message)
                .system(promptSystemSpec -> {
                    promptSystemSpec.param("current_date", LocalDate.now());
                })
                .advisors(advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20))
                .stream()
                .content();
        return content.concatWith(Flux.just("[complete]"));
    }

}
