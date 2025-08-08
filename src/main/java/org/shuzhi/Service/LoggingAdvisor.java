package org.shuzhi.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.prompt.Prompt;

import reactor.core.publisher.Flux;

public class LoggingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAdvisor.class);

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        Prompt prompt = advisedRequest.toPrompt();
        logger.info("拦截到Prompt请求: {}", prompt.getContents());
        logger.debug("Prompt选项: {}", JSONObject.toJSONString(prompt.getOptions()));
        return chain.nextAroundCall(advisedRequest);
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        Prompt prompt = advisedRequest.toPrompt();
        logger.info("拦截到流式Prompt请求: {}", prompt.getContents());
        logger.debug("流式Prompt选项: {}", JSON.toJSONString(prompt.getOptions()));
        return chain.nextAroundStream(advisedRequest);
    }

    @Override
    public String getName() {
        return "loggingAdvisor";
    }

    @Override
    public int getOrder() {
        // 返回一个合适的顺序值，这里使用默认的最低优先级
        return Integer.MAX_VALUE;
    }
}
