
package MafiaG;

import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ConGemini {
private static final String GEMINI_API_KEY = "AIzaSyBMbNZD6Q_zmzErjpfK_l9Ti7FMtzYAadA";
private static final String GEMINI_URL = "<https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent?key=>";

private static final OkHttpClient client = new OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build();

public static String getResponse(String userPrompt) throws IOException {
    MediaType mediaType = MediaType.parse("application/json");

    String requestBody = "{\n" +
            "  \"contents\": [\n" +
            "    {\n" +
            "      \"parts\": [\n" +
            "        {\n" +
            "          \"text\": \"" + userPrompt + "\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ],\n" +
            "  \"generationConfig\": {\n" +
            "    \"temperature\": 0.5\n" +
            "  }\n" +
            "}";


    Request request = new Request.Builder()
            .url(GEMINI_URL + GEMINI_API_KEY)
            .post(RequestBody.create(mediaType, requestBody))
            .addHeader("Content-Type", "application/json")
            .build();

    try (Response response = client.newCall(request).execute()) {
        String responseBody = response.body().string();

        if (!response.isSuccessful()) {
            return "오류 발생: " + response.code() + "\n상세 내용: " + responseBody;
        }

        return extractTextFromResponse(responseBody);
    }
}

private static String extractTextFromResponse(String json) {
    int start = json.indexOf("\"text\": \"") + 9;
    int end = json.indexOf("\"", start);
    if (start != -1 && end != -1 && end > start) {
        return json.substring(start, end).replace("\\n", "\n");
    }
    return "응답 파싱 실패: " + json;
}
}