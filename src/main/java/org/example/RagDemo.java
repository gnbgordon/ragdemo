package org.example;

import com.google.common.collect.Maps;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.aggregator.DefaultContentAggregator;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.rag.query.transformer.ExpandingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * chroma向量库
 */
public class RagDemo {
    public static void main(String[] args) {
        //向量模型
        OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .baseUrl("https://api.chatanywhere.tech/v1")
                .apiKey("sk-sjXwsAFOtfY1UPpuUdzkxIwQr78UpsCOaIysPJCTsSuROGdo")
                .build();
        //向量存储
        /*RedisEmbeddingStore embeddingStore = RedisEmbeddingStore.builder()
                .host("127.0.0.1")
                .port(6379)
                .dimension(1536)
                .build();*/
        // 向量存储
        ChromaEmbeddingStore embeddingStore = ChromaEmbeddingStore.builder()
                .collectionName("my_collection") // 设置集合名称
                .baseUrl("http://localhost:8000") // 设置 Chroma 服务器 URL
                .build();


        //内容检索器
        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(3)
                .minScore(0.8)
                .build();

        // 创建模型
        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .baseUrl("https://api.chatanywhere.tech/v1")
                .apiKey("sk-sjXwsAFOtfY1UPpuUdzkxIwQr78UpsCOaIysPJCTsSuROGdo")
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

        Query query = new Query("余额提现什么时候到账？");
        //Query query = new Query("在线支付取消订单后钱怎么返还？");

        //查询转换器
        QueryTransformer queryTransformer = new ExpandingQueryTransformer(chatModel, 1);
        Collection<Query> queries = queryTransformer.transform(query);

        //查询路由器
        QueryRouter queryRouter = new LanguageModelQueryRouter(chatModel, Map.of(contentRetriever, "美团外卖常见问题"));
        Map<Query, Collection<List<Content>>> queryToContents = Maps.newHashMap();
        for (Query query1 : queries) {
            //内容检索器
            Collection<ContentRetriever> retrievers = queryRouter.route(query1);
            for (ContentRetriever retriever : retrievers) {
                List<Content> contents = retriever.retrieve(query1);
                queryToContents.put(query1, Collections.singleton(contents));
            }
        }

        //内容增强器
        DefaultContentAggregator defaultContentAggregator = new DefaultContentAggregator();
        List<Content> contents = defaultContentAggregator.aggregate(queryToContents);
        //内容注入器,提示词
        ContentInjector contentInjector = new DefaultContentInjector();
        dev.langchain4j.data.message.UserMessage userMessage = contentInjector.inject(contents, dev.langchain4j.data.message.UserMessage.userMessage(query.text()));
        //调用大模型,获取内容
        Response<AiMessage> response = chatModel.generate(userMessage);
        System.out.println(response.content().text());
    }


    public static Document getDocument() {
        // 加载并解析文件
        Document document;
        try {
            Path documentPath = Paths.get(RagDemo.class.getClassLoader().getResource("meituan-qa.txt").toURI());
            DocumentParser documentParser = new TextDocumentParser();
            document = FileSystemDocumentLoader.loadDocument(documentPath, documentParser);
            return document;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}