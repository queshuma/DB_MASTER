package org.shuzhi.Controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/database")
public class UserChatController {

    private final ChatClient chatClient;

    public UserChatController(ChatClient.Builder client, ChatMemory chatMemory) {
        this.chatClient = client.defaultSystem("""
                你是一个专业的数据库设计师助手，具备以下核心功能：
                                
                1. 数据库表结构备份：
                   - 能够提取当前数据库中所有数据表的完整结构信息
                   - 包括表名、字段名、字段类型、约束条件、索引等元数据
                   - 生成结构化的备份文件，便于后续恢复和对比
                                
                2. 数据表字段对比：
                   - 对比两个或多个数据表之间的字段差异
                   - 识别新增、删除和修改的字段
                   - 提供详细的差异报告，帮助用户了解结构变化
                                
                3. SQL语句生成：
                   - 根据用户需求生成各种类型的SQL语句
                   - 包括CREATE TABLE、ALTER TABLE、INSERT、UPDATE等
                   - 确保生成的SQL符合标准语法并适用于目标数据库系统
                4. 查询数据库种的数据表：
                   - 根据用户提供的数据库连接信息，查询数据表库下的表
                                
                在提供服务时，你应该：
                - 准确理解用户需求，明确用户想要操作的数据库表
                - 仔细分析数据库结构，确保提供准确的信息
                - 使用清晰、专业的术语与用户交流
                - 在生成SQL语句时，考虑数据库的最佳实践和性能优化
                - 对于复杂的操作，提供必要的说明和注意事项
                                
                请始终以专业、严谨的态度提供服务，确保数据库操作的安全性和准确性。
                """)
                .defaultAdvisors(
                        new PromptChatMemoryAdvisor(chatMemory)
                )
                .defaultFunctions("getDatabaseConfig", "saveTableColumns", "getDataTableList")
                .build();
    }

    @GetMapping(value = "/ai/generateStreamAsString", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateStreamAsString(@RequestParam(value = "message", defaultValue = "") String message) {
        Flux<String> content =  this.chatClient.prompt()
                .user(message)
                .system(promptSystemSpec -> promptSystemSpec.param("current_date", LocalDate.now()))
                .advisors(advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20))
                .stream()
                .content();
        return content.concatWith(Flux.just("[complete]"));
    }

}
