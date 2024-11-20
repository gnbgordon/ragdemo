package org.example;

import dev.langchain4j.classification.EmbeddingModelTextClassifier;
import dev.langchain4j.classification.TextClassifier;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 客服问题分类示例
 */
public class CustomerServiceCategoryDemo {
    enum CustomerServiceCategory {
        订单状态,
        技术支持,
        用户反馈
    }

    public static void main(String[] args) {
        Map<CustomerServiceCategory, List<String>> examples = Map.of(
                CustomerServiceCategory.订单状态, Arrays.asList(
                        "我的订单在哪里？",
                        "更新一下发货状态。"
                ),
                CustomerServiceCategory.技术支持, Arrays.asList(
                        "应用程序启动时崩溃。",
                        "连接服务器出现问题。"
                        ),
                CustomerServiceCategory.用户反馈, Arrays.asList(
                        "服务很好，谢谢！",
                        "客服需要改进。"
                )
        );

        // 嵌入模型
        OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .baseUrl("https://api.chatanywhere.tech/v1")
                .apiKey("sk-sjXwsAFOtfY1UPpuUdzkxIwQr78UpsCOaIysPJCTsSuROGdo")
                .build();

        // 创建分类器实例
        TextClassifier<CustomerServiceCategory> classifier =
                new EmbeddingModelTextClassifier<>(embeddingModel, examples);

        // 对一条新消息进行分类
        List<CustomerServiceCategory> categories = classifier.classify("为什么应用程序这么慢？");

        // 输出分类结果
        System.out.println(categories); // [技术支持]
    }

}
