package com.pannous.jini.openai;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.pannous.jini.settings.AppSettingsConfigurable;
import com.pannous.jini.settings.AppSettingsState;
import com.pannous.jini.settings.Options;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.service.OpenAiService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static com.pannous.jini.openai.OpenAITools.getKey;
import static com.pannous.jini.settings.Options.replace;

public class OpenAI2 {

    public static void strim() {

        String token = System.getenv("OPENAI_TOKEN");
        OpenAiService service = new OpenAiService(token);

        System.out.println("\nCreating completion...");
        CompletionRequest completionRequest = CompletionRequest.builder()
                .model("ada")
                .prompt("Somebody once told me the world is gonna roll me")
                .echo(true)
                .user("testing")
                .n(3)
                .build();
        service.createCompletion(completionRequest).getChoices().forEach(System.out::println);

        System.out.println("\nCreating Image...");
        CreateImageRequest request = CreateImageRequest.builder()
                .prompt("A cow breakdancing with a turtle")
                .build();

        System.out.println("\nImage is located at:");
        System.out.println(service.createImage(request).getData().get(0).getUrl());

        System.out.println("Streaming chat completion...");
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are a dog and will speak as such.");
        messages.add(systemMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .n(1)
                .maxTokens(50)
                .logitBias(new HashMap<>())
                .build();

        service.streamChatCompletion(chatCompletionRequest)
                .doOnError(Throwable::printStackTrace)
                .blockingForEach(System.out::println);
    }

    public static void query(Project project, Prompt prompt, String code, String language, Consumer<String> callback, Options options) {
        if (code.length() > 3000) code = code.substring(0, 3000); // todo split / notify
        AppSettingsState settings = AppSettingsState.getInstance();
        if (settings != null && (settings.OPENAI_API_KEY == null || settings.OPENAI_API_KEY.isEmpty())) {
            getKey(project);
            return;
        }

        List<ChatMessage> chatMessages = new ArrayList<>();
        if (prompt == Prompt.EXECUTE) {
            chatMessages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), "OS: " + System.getProperty("os.name")));
            chatMessages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), "ARCH: " + System.getProperty("os.arch")));
        } else {
            chatMessages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), "Language: " + language));
        }
        chatMessages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), prompt.getText()));
        if (prompt == Prompt.CONVERT)
            chatMessages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), "Target language: " + prompt.language));
        chatMessages.add(new ChatMessage(ChatMessageRole.USER.value(), code));
        if (options.has(replace) || options.has(Options.noExplanations)) {
            chatMessages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), "DO NOT OUTPUT ANY COMMENTS OR EXPLANATIONS"));
        }

        fetchInBackground(callback, prompt.name(), chatMessages, project, code);
    }


    public static void fetchInBackground(Consumer<String> callback, String text, List<ChatMessage> chatMessages, Project project, String code) {
        String openAiKey = getKey(project);
        Task.Backgroundable task = new Task.Backgroundable(project, "Get GPT answer", true) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                progressIndicator.setIndeterminate(true);
                progressIndicator.setText(text);
                if (!progressIndicator.isRunning())
                    progressIndicator.start();
                try {// Perform long-running operation
                    OpenAiService service = new OpenAiService(openAiKey, Duration.ofSeconds(60));
                    ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                            .model("gpt-3.5-turbo")
                            .messages(chatMessages)
//                            .frequencyPenalty(.5)
                            .temperature(.2)
                            .maxTokens(3500)
                            .build();
                    String result = service.createChatCompletion(completionRequest).getChoices().get(0).getMessage().getContent();
                    progressIndicator.stop();
                    callback.accept(result);
                } catch (Exception e) {
                    System.err.println("OPENAI API ERROR");
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        ProgressManager.getInstance().run(task);
    }


}
