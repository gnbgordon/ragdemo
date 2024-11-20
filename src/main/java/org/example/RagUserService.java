package org.example;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.zhipu.ZhipuAiChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 使用AiService来实现美团智能客服
 */
public class RagUserService {
    public interface CustomerServiceAgent {
        // 用来回答问题的方案,定义了一个 answer 方法，用于根据用户ID和问题生成AI回答。
        Response<AiMessage> answer(@MemoryId Integer userId, @UserMessage String question);
    }

    public static void main(String[] args) {
        //向量模型,初始化向量模型：使用 OpenAI 的嵌入模型
        OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .baseUrl("https://api.chatanywhere.tech/v1")
                .apiKey("sk-sjXwsAFOtfY1UPpuUdzkxIwQr78UpsCOaIysPJCTsSuROGdo")
                .build();
        //向量存储
       /* RedisEmbeddingStore embeddingStore = RedisEmbeddingStore.builder()
                .host("127.0.0.1")
                .port(6379)
                .dimension(1536)
                .build();*/

        //初始化向量存储：使用 Chroma 向量存储
        ChromaEmbeddingStore embeddingStore = ChromaEmbeddingStore.builder()
                .collectionName("my_collection") // 设置集合名称
                .baseUrl("http://localhost:8000") // 设置 Chroma 服务器 URL
                .build();

        //内容检索器,创建内容检索器：基于嵌入模型和向量存储，设置最大结果数和最小分数
        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(3)
                .minScore(0.8)
                .build();

        // 创建模型,创建聊天模型：使用 OpenAI 的聊天模型
        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .baseUrl("https://api.chatanywhere.tech/v1")
                .apiKey("sk-sjXwsAFOtfY1UPpuUdzkxIwQr78UpsCOaIysPJCTsSuROGdo")
                .build();

        //创建增强器：使用内容检索器
        DefaultRetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .contentRetriever(contentRetriever)
                .build();

        //创建AiService 代理,创建 AI 服务代理：配置聊天模型、增强器、聊天记忆和工具
        CustomerServiceAgent agent = AiServices.builder(CustomerServiceAgent.class)
                .chatLanguageModel(chatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemoryProvider(userId -> MessageWindowChatMemory.withMaxMessages(10))
                .tools(new DateCalculator())
                .build();

        //加载文件
        Document document = getDocument();
        // 切分文件
        DocumentSplitter splitter = new CustomerServiceDocumentSplitter();
        List<TextSegment> segments = splitter.split(document);
        //生成向量
        List<Embedding> content = embeddingModel.embedAll(segments).content();
        //存储向量
        embeddingStore.addAll(content, segments);


        Query query = new Query("今天的余额提现，最晚哪天能到账？给我具体的日期,什么时间？");
        //调用大模型
        Response<AiMessage> answer = agent.answer(1, query.text());
        System.out.println(answer.content().text());
        System.out.println(answer.tokenUsage());
    }

    public static Document getDocument() {
        // 加载并解析文件
        Document document;
        try {
            Path documentPath = Paths.get(RagUserService.class.getClassLoader().getResource("meituan-qa.txt").toURI());
            DocumentParser documentParser = new TextDocumentParser();
            document = FileSystemDocumentLoader.loadDocument(documentPath, documentParser);
            return document;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
