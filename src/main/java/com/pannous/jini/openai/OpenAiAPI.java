package com.pannous.jini.openai;

import com.pannous.jini.settings.ApiKeys;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;

public class OpenAiAPI {

    private static final URI gpt_service = URI.create("https://api.openai.com/v1/chat/completions");
    private static final URI completion_service = URI.create("https://api.openai.com/v1/engines/davinci/completions");


    public String postToOpenAiApi(String requestBodyAsJson, URI service)
            throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder().uri(service)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + ApiKeys.OPENAI_API_KEY)
                .POST(BodyPublishers.ofString(requestBodyAsJson)).build();
        final HttpClient client = HttpClient.newHttpClient();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public String complete(String start) throws IOException, InterruptedException {
        String requestBodyAsJson = "{\n" +
                "  \"prompt\": \"This is a test\",\n" +
                "  \"max_tokens\": 5,\n" +
                "  \"temperature\": 0.9,\n" +
                "  \"top_p\": 1,\n" +
                "  \"n\": 1,\n" +
                "  \"stream\": false,\n" +
                "  \"logprobs\": null,\n" +
                "  \"stop\": \"\\n\"\n" +
                "}";
        String s = postToOpenAiApi(requestBodyAsJson, completion_service);
        return s;
    }

    public String query(String content) throws IOException, InterruptedException {
        String messages;
        if (content.startsWith("["))
            messages = content;
        else {
            String safe_message = content.replace("\"", "'").replace("\n", "\\n");
//            we could use "content":``` + safe_message + "``` but then we need to escape the backticks in the content
            messages = "[{\"role\": \"user\", \"content\": \"" + safe_message + "\"}]";
        }

        String json = "{\n" +
                "     \"model\": \"gpt-3.5-turbo\",\n" +
                "     \"messages\":" + messages + ",\n" +
                "     \"temperature\": 0.3\n" +
                "   }";
        String response = postToOpenAiApi(json, gpt_service);
        int start = response.indexOf("content");
        if (start == -1) return "ERROR\n" + json + "\n" + response;
        response = response.substring(start + 10, response.indexOf("\"}", start));
        response = response.replace("As an AI language model, ", "");
//        JsonReader reader = Json.createReader(new StringReader(response));
//JsonObject jsonObject = reader.readObject();
        // parse response json

        return response.replace("\\n", "\n").trim();

    }

}
