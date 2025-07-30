package org.shuzhi.Controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Description:
 * Author: chentao
 * Date: 19 7月 2025
 *
 * @version 1.0
 */
@RestController
@RequestMapping("/chat")
public class DashScopeChatModelController {
    private final ChatModel dashScopeChatModel;

    public DashScopeChatModelController(ChatModel chatModel) {
        this.dashScopeChatModel = chatModel;
    }

    private static final String DEFAULT_PROMPT = "你好，我是AI助手，你有什么问题想问我吗？";

    @GetMapping("/simple/chat")
    public String simpleChat() {

        return dashScopeChatModel.call(new Prompt(DEFAULT_PROMPT)).getResult().getOutput().getContent();
    }

    /**
     * Stream 流式调用。可以使大模型的输出信息实现打字机效果。
     * @return Flux<String> types.
     */
    @GetMapping("/stream/chat")
    public Flux<String> streamChat(HttpServletResponse response) {

        // 避免返回乱码
        response.setCharacterEncoding("UTF-8");

        Flux<ChatResponse> stream = dashScopeChatModel.stream(new Prompt(DEFAULT_PROMPT));
        return stream.map(resp -> resp.getResult().getOutput().getContent());
    }

    @GetMapping("/custom/chat")
    public String customChat() {

        DashScopeChatOptions customOptions = DashScopeChatOptions.builder()
                .withTopP(0.7)
                .withTopK(50)
                .withTemperature(0.8)
                .withModel("xxx")
                .build();

        return dashScopeChatModel.call(new Prompt(DEFAULT_PROMPT, customOptions)).getResult().getOutput().getContent();
    }
}
