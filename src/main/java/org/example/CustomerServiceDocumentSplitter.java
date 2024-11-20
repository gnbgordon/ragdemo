package org.example;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;

import java.util.ArrayList;
import java.util.List;

/**
 * 该类的主要功能是将文档拆分成多个文本段落
 */
public class CustomerServiceDocumentSplitter implements DocumentSplitter {

    @Override
    /**
     * 根据文档中的空白行分割文档内容
     *
     * 此方法旨在处理文档分割操作，将一个文档根据其中的空白行分割成多个文本段落
     * 每个段落表示文档中由空白行分隔的部分
     *
     * @param document 待分割的文档对象
     * @return 返回一个包含多个文本段落的列表
     */
    public List<TextSegment> split(Document document) {

        // 创建一个列表，用于存储分割后的文本段落
        List<TextSegment> segments = new ArrayList<>();

        // 调用split方法，根据文档内容中的空白行进行分割
        String[] parts = split(document.text());
        for (String part : parts) {
            // 将分割后的每个部分转换为TextSegment对象，并添加到列表中
            segments.add(TextSegment.from(part));
        }

        // 返回包含所有文本段落的列表
        return segments;
    }

    /**
     * 根据输入文本中的空白行进行分割
     *
     * 此方法的目的是按照文本中的空白行来分割文本，返回一个包含所有分割部分的字符串数组
     * 空白行被视为段落的分隔符，用于确定文本分割的位置
     *
     * @param text 待分割的文本内容
     * @return 返回一个字符串数组，包含所有分割后的文本部分
     */
    public String[] split(String text) {
        // 使用正则表达式分割文本，\\s*\\R\\s*\\R\\s*匹配一个或多个空白字符，包括行结束符
        // 这里假设文档中的空白行是段落的分隔符
        return text.split("\\s*\\R\\s*\\R\\s*");
    }

}
