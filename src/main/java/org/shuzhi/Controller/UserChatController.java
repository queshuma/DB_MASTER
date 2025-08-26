package org.shuzhi.Controller;


import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/database")
public class UserChatController {
//
//    private final String DEFAULT_PROMPT = "" +
//            "                你是一个专业的数据库设计师助手，具备以下核心功能：\n" +
//            "                1. 创建项目，请让用户输入项目名称，项目描述，项目类型，在创建前请询问用户是否确认\n" +
//            "                2. 项目创建完后，需要提供给用户项目信息，或者要做其他操作，需要用户完善数据库配置\n" +
//            "                2. 完善数据库，根据项目id进行更新\n" +
//            "                2. 获取项目列表，查询项目列表，返回的数据通过颜表情进行区分\n" +
//            "                3. 查询项目的数据库信息，根据项目的编号或者名称查询，查询前询问用户，确认是通过编号还是名称查询\n" +
//            "                4. 备份项目数据结构前 ，需要用户确认项目信息以及数据库信息，并提供版本号" +
//            "                5  查询项目的备份记录，根据项目的编号或者名称查询，查询前询问用户，确认是通过编号还是名称查询\n" +
//            "                6. 查询项目的数据库配置，查询当前的数据了有哪些" +
//            "                7. 数据库字段对比，根据两个版本的字段设计，获取字段的调整" +
//            "                8. 比较两个版本的数据表的差异,请用户提供两个版本号，分别是原版本、新版本" +
//            "                9. 比较两个版本的数据字段的差异，请用户提供两个版本号，分别是原版本、新版本";
//
//
//
//    private final ChatClient chatClient;
//
//    public UserChatController(ChatClient.Builder client, ChatMemory chatMemory) {
//        this.chatClient = client.defaultSystem(DEFAULT_PROMPT)
//                .defaultAdvisors(
//                        new PromptChatMemoryAdvisor(chatMemory),
//                        new LoggingAdvisor()
//                )
//                .defaultFunctions("createProject", "getProjectList", "updateProjectData", "getProjectDataBase"
//                        , "getProjectHistory", "getDataTableList", "backupData", "compareColumn", "compareTable")
//                .build();
//    }
//
//    @GetMapping(value = "/ai/generateStreamAsString", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<String> generateStreamAsString(@RequestParam(value = "message", defaultValue = "") String message) {
//        Flux<String> content =  this.chatClient.prompt()
//                .user(message)
//                .system(promptSystemSpec -> {
//                    promptSystemSpec.param("current_date", LocalDate.now());
//                })
//                .advisors(advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20))
//                .stream()
//                .content();
//        return content.concatWith(Flux.just("[complete]"));
//    }

}
