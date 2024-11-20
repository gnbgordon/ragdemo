package org.example;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.model.output.Response;

import static dev.langchain4j.model.openai.OpenAiModelName.DALL_E_3;

/**
 * 文生图示例，免费API限制使用gpt-3.5-turbo，gpt-4 和 embeddings模型,这个https://api.gptsapi.net/v1可用
 */
public class ImageModelDemo {
    public static void main(String[] args){
        //构建ImageModel模型
        /*ImageModel model = OpenAiImageModel.builder()
                .baseUrl("https://api.chatanywhere.tech/v1")
                .apiKey("sk-sjXwsAFOtfY1UPpuUdzkxIwQr78UpsCOaIysPJCTsSuROGdo")
                .modelName(DALL_E_3)
                .build();*/
        ImageModel model = OpenAiImageModel.builder()
                .baseUrl("https://api.gptsapi.net/v1")
                .apiKey("sk-Qxt1e040220a75c18e3a2193f6b5cb0d8fb718b7ff3uxxI1")
                .modelName(DALL_E_3)
                .build();

        //生成图像
        Response<Image> response = model.generate("黄昏日落，日式动漫风格");
        System.out.println(response.content().url());

    }
}
