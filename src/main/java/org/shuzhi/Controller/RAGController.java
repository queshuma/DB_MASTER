package org.shuzhi.Controller;

import cn.dev33.satoken.stp.StpUtil;
import org.shuzhi.Service.DatabaseMetadataService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/database")
public class RAGController {

    public static final String COMPLETE = "[complete]";
    private final String DEFAULT_PROMPT = """
        你是一位专业的数据库设计师助手 (｡◕‿◕｡)  
        请严格按照以下规范输出：  
//        1. 以自然语言开头总结项目信息，并换行。  
        1. 使用 Markdown 表格展示项目数据，每行一条记录，必须包含表头和分隔行。  
        2. 使用换行符分隔每行。  
        3. 在结尾提供操作提示，可带颜文字。  
        
        ================ 核心功能 ================  
        - 查询项目列表：调用工具获取数据。  
        - 创建项目：需用户输入名称、描述、类型，创建前先确认。  
        - 项目创建后：展示项目信息，并提示是否完善数据库配置或其他操作。  
        - 完善数据库配置：根据 ID 查询；存在则确认并更新，不存在则提示先创建。  
        - 查询数据库信息：通过项目编号或名称查询项目的数据库信息。  
        - 备份项目结构：需用户确认项目信息和数据库信息，并提供版本号。  
        - 查询备份记录：编号或名称查询，先确认方式。  
        - 查询数据库配置：根据项目id或者项目名称查询数据库配置。  
        - 字段对比：根据两个版本字段设计，输出调整方案。  
        - 表结构对比：需用户提供原/新版本号，输出差异。  
        - 字段差异对比：需用户提供原/新版本号，输出差异。  
        
        ================ 知识库功能 ================  
        - 如果不是操作问题，请查询知识库。  
//        - 若问题无关，请礼貌回绝 (＞﹏＜)。  
        """;



    private ChatClient chatClient;

    private VectorStore vectorStore;

    @Autowired
    private DatabaseMetadataService databaseMetadataService;

    public RAGController(ChatClient.Builder client, ChatMemory chatMemory, VectorStore vectorStore) {
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
                // 注册所有 Tool 服务（自动识别 @Tool 方法）
                .tools(databaseMetadataService)
                .toolContext(Map.of(
                        "userId", StpUtil.getLoginId(),
                        "satoken", StpUtil.getTokenValue()
                ))
                // 添加问答适配器
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                // 构建响应流
                .stream()
                .content();

        return result.concatWith(Flux.just(COMPLETE));
    }
}
