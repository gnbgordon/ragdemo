package org.example;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Demo1 {
    public static void main1(String[] args) {
        try {

            String baseUrl = "https://api.chatanywhere.tech/v1/chat/completions";
            String apiKey = "sk-sjXwsAFOtfY1UPpuUdzkxIwQr78UpsCOaIysPJCTsSuROGdo";
            URL url = new URL(baseUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String inputJson = "{\n" +
                    "\"model\": \"gpt-3.5-turbo\",\n" +
                    "\"messages\": [{\"role\": \"user\", \"content\": \"你好\"}],\n" +
                    "\"temperature\": 0.7\n" +
                    "}";
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = inputJson.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            // 处理响应...

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
