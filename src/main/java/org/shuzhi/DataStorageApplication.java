package org.shuzhi;

import cn.dev33.satoken.SaManager;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingModel;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.CrossOrigin;
import redis.clients.jedis.JedisPooled;

@SpringBootApplication
@MapperScan("org.shuzhi.Mapper")
@CrossOrigin
@EnableAsync(proxyTargetClass = true)
public class DataStorageApplication {


    private static final Logger logger = LoggerFactory.getLogger(DataStorageApplication.class);

    public static void main(String[] args) {

        SpringApplication.run(DataStorageApplication.class, args);
        System.out.println("启动成功，Sa-Token 配置如下：" + SaManager.getConfig());
    }

    @Bean
    public JedisPooled jedisPooled() {
        return new JedisPooled("49.232.61.41", 6379, null, "redisadmin");
    }

    @Bean
    public VectorStore vectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName("custom-index")                // Optional: defaults to "spring-ai-index"
                .prefix("custom-prefix")                  // Optional: defaults to "embedding:"
                .metadataFields(                         // Optional: define metadata fields for filtering
                        RedisVectorStore.MetadataField.tag("country"),
                        RedisVectorStore.MetadataField.numeric("year"))
                .initializeSchema(true)                   // Optional: defaults to false
                .batchingStrategy(new TokenCountBatchingStrategy()) // Optional: defaults to TokenCountBatchingStrategy
                .build();
    }

//    @Bean
//    public EmbeddingModel embeddingModel() {
//        ZhiPuAiApi zhiPuAiApi = new ZhiPuAiApi("e6c75ac4eeda47a1a426501e371bbda3.u5Xnrt5MGhKDAxgt");
//        return new ZhiPuAiEmbeddingModel(zhiPuAiApi); // 🔑 智谱 API Key
//    }
}
