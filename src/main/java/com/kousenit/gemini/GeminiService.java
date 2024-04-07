package com.kousenit.gemini;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import static com.kousenit.gemini.JsonStructure.*;

@Service
public class GeminiService {
    public static final String GEMINI_PRO = "gemini-pro";
    public static final String GEMINI_ULTIMATE = "gemini-ultimate";
    public static final String GEMINI_PRO_VISION = "gemini-pro-vision";

    private final GeminiInterface geminiInterface;

    @Autowired
    public GeminiService(GeminiInterface geminiInterface) {
        this.geminiInterface = geminiInterface;
    }

    public ModelList getModels() {
        return geminiInterface.getModels();
    }

    public GeminiResponse getCompletion(GeminiRequest request) {
        return geminiInterface.getCompletion(GEMINI_PRO, request);
    }

    public GeminiResponse getCompletionWithModel(String model, GeminiRequest request) {
        return geminiInterface.getCompletion(model, request);
    }


    public GeminiResponse getCompletionWithImage(GeminiRequest request) {
        return geminiInterface.getCompletion(GEMINI_PRO_VISION, request);
    }

    public String getCompletion(String text) {
        GeminiResponse response = getCompletion(new GeminiRequest(
                List.of(new Content(List.of(new TextPart(text))))));
        return response.candidates().getFirst().content().parts().getFirst().text();
    }

    public String getCompletionWithImage(String text, String imageFileName) throws IOException {
        GeminiResponse response = getCompletionWithImage(
                new GeminiRequest(List.of(new Content(List.of(
                        new TextPart(text),
                        new InlineDataPart(new InlineData("image/png",
                                Base64.getEncoder().encodeToString(Files.readAllBytes(
                                        Path.of("src/main/resources/", imageFileName))))))
                ))));
        System.out.println(response);
        return response.candidates().getFirst().content().parts().getFirst().text();
    }
}
