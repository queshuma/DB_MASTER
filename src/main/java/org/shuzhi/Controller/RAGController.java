package org.shuzhi.Controller;

import org.shuzhi.Service.DatabaseMetadataService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
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

    private final String DEFAULT_PROMPT = "" +
            "                你是一个专业的数据库设计师助手，可以搭配颜文字内容以及Markdown的格式，具备以下核心功能：\n" +
            "                ============== 业务功能 ==============" +
            "                1. 查询项目列表,需要调用查询项目列表的工具获取数据\n" +
            "                2. 创建项目，请让用户输入项目名称，项目描述，项目类型，在创建前请询问用户是否确认\n" +
            "                3. 项目创建完后，需要提供给用户项目信息，或者要做其他操作，需要用户完善数据库配置\n" +
            "                4. 完善数据库，根据项目id进行更新\n" +
            "                5. 查询项目的数据库信息，根据项目的编号或者名称查询，查询前询问用户，确认是通过编号还是名称查询\n" +
            "                6. 备份项目数据结构前 ，需要用户确认项目信息以及数据库信息，并提供版本号" +
            "                7  查询项目的备份记录，根据项目的编号或者名称查询，查询前询问用户，确认是通过编号还是名称查询\n" +
            "                8. 查询项目的数据库配置，查询当前的数据了有哪些" +
            "                9. 数据库字段对比，根据两个版本的字段设计，获取字段的调整" +
            "                10. 比较两个版本的数据表的差异,请用户提供两个版本号，分别是原版本、新版本" +
            "                11. 比较两个版本的数据字段的差异，请用户提供两个版本号，分别是原版本、新版本" +
            "                ============== 知识库功能 ==============" +
            "                当询问到一些数据库相关的技术、知识的时候，你会主动的去查询知识库中的内容，如果是询问与知识库无关的内容，请礼貌友好的回绝";



    private ChatClient chatClient;

    private VectorStore vectorStore;

    private DatabaseMetadataService databaseMetadataService;

    List <Document> documents = List.of(
            new Document("MySQL：全球最流行的开源关系型数据库，以易用性、社区活跃和广泛的互联网应用生态著称。\n" +
                    "\n" +
                    "GBase：源自中国、主打国产化和分析型场景的数据库系列，更强调在特定领域（如数据分析、政府、金融关键业务）的高性能、高安全和高可靠性。", Map.of("数据库区别", "mysql和gbase")));



    public RAGController(ChatClient.Builder client, ChatMemory chatMemory, VectorStore vectorStore, DatabaseMetadataService databaseMetadataService) {
        this.vectorStore = vectorStore;
        this.databaseMetadataService = databaseMetadataService;
        this.chatClient = client.defaultSystem(DEFAULT_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
//                        new LoggingAdvisor()
                )
//                .defaultTools(DatabaseMetadataService.class)
                .build();
        vectorStore.add(documents);
    }

    @GetMapping(value = "/ai/generateStreamAsString", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateStreamAsString(@RequestParam(value = "message", defaultValue = "") String message) {
        Flux<String> content =  this.chatClient.prompt()
                .user(message)
                .system(promptSystemSpec -> {
                    promptSystemSpec.param("current_date", LocalDate.now());
                })
                .tools(databaseMetadataService)
                .advisors(
                        new QuestionAnswerAdvisor(vectorStore)
                )
                .stream()
                .content();
        return content.concatWith(Flux.just("[complete]"));
    }

}
