package org.shuzhi.Controller;

import cn.dev33.satoken.stp.StpUtil;
import org.shuzhi.Config.ReactiveContext;
import org.shuzhi.Service.DatabaseMetadataService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/database")
public class RAGController {

    public static final String COMPLETE = "[complete]";
    private final String DEFAULT_PROMPT = """
    输出内容要求严格遵循以下规范：
    1. 自然语言开头总结项目信息，然后换行
    2. 用 Markdown 表格展示项目数据
    3. 每行一条记录，表头和分隔行必须包含
    4. 使用换行符分隔每行
    5. 在结尾提供操作提示

    你是一个专业的数据库设计师助手，可以搭配颜文字内容，具备以下核心功能：
    ============= 业务功能 ==============
    1. 查询项目列表，需要调用查询项目列表的工具获取数据
    2. 创建项目，请让用户输入项目名称，项目描述，项目类型，在创建前请询问用户是否确认
    3. 项目创建完后，需要提供给用户项目信息，或者要做其他操作，需要用户完善数据库配置
    4. 完善数据库配置，需要先根据id查询是否有该项目，如果有，那么需要用户确认项目信息后，然后根据项目id进行更新，如果没有则让用户创建项目
    5. 查询项目的数据库信息，根据项目的编号或者名称查询，查询前询问用户，确认是通过编号还是名称查询
    6. 备份项目数据结构前，需要用户确认项目信息以及数据库信息，并提供版本号
    7. 查询项目的备份记录，根据项目的编号或者名称查询，查询前询问用户，确认是通过编号还是名称查询
    8. 查询项目的数据库配置，查询当前的数据有哪些
    9. 数据库字段对比，根据两个版本的字段设计，获取字段的调整
    10. 比较两个版本的数据表的差异，请用户提供两个版本号，分别是原版本、新版本
    11. 比较两个版本的数据字段的差异，请用户提供两个版本号，分别是原版本、新版本

    ============= 知识库功能 ==============
    当询问到一些数据库相关的技术、知识的时候，你会主动的去查询知识库中的内容，如果是询问与知识库无关的内容，请礼貌友好的回绝
    """;



    private ChatClient chatClient;

    private VectorStore vectorStore;

    @Autowired
    private DatabaseMetadataService databaseMetadataService;

    List<Document> documents = List.of(
            new Document("MySQL：全球最流行的开源关系型数据库，以易用性、社区活跃和广泛的互联网应用生态著称。\n" +
                    "\n" +
                    "GBase：源自中国、主打国产化和分析型场景的数据库系列，更强调在特定领域（如数据分析、政府、金融关键业务）的高性能、高安全和高可靠性。", Map.of("数据库区别", "mysql和gbase")));


    public RAGController(ChatClient.Builder client, ChatMemory chatMemory, VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.chatClient = client.defaultSystem(DEFAULT_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
        vectorStore.add(documents);
    }

    @GetMapping(value = "/ai/generateStreamAsString", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateStreamAsString(@RequestParam(value = "message", defaultValue = "") String message) {
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
//                .chatResponse()
                // 在每个响应中确保上下文正确设置
//                .flatMap(resp -> Mono.just(resp))
//                // 最终追加结束标识
//                .map(response -> {
//                    // 可选：将用户信息注入到响应中
//                    System.out.println(Optional.ofNullable(response.getResult().getOutput().getText()).orElse(""));
//                    return Optional.ofNullable(response.getResult().getOutput().getText()).orElse("");
//                });
        return result
                // 添加结束标识
                .concatWith(Flux.just(COMPLETE));
    }
}
