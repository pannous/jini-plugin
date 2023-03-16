package com.pannous.jini.openai;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;

import com.pannous.jini.settings.Options;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.pannous.jini.settings.AppSettingsConfigurable;
import com.pannous.jini.settings.AppSettingsState;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.pannous.jini.settings.Options.replace;

public class OpenAI {

    private static String getKey(@NotNull Project project) {
        AppSettingsState settings = AppSettingsState.getInstance();
        if (settings.OPENAI_API_KEY != null && !settings.OPENAI_API_KEY.isEmpty())
            return settings.OPENAI_API_KEY;
        String key = null;// enter via settings
        // Messages.showInputDialog("Please set your OpenAI API key in the Jini settings", "OpenAI API key", Messages.getQuestionIcon());
        ConfigurableProvider provider = new ConfigurableProvider() {
            @Nullable
            @Override
            public Configurable createConfigurable() {
                return new AppSettingsConfigurable();
            }
        };
        if (key != null && !key.isEmpty()) {
            settings.OPENAI_API_KEY = key;
            PersistentStateComponent<AppSettingsState> component = settings;
            component.getState();// SAVEs it
        } else {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, "Jini Settings");
        }
        return key;
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

        fetchInBackground(callback, prompt.name(), chatMessages, project,code);
    }

    public static String extractCode(String result) {
        if(result.startsWith("```"))
            result = result.substring( result.indexOf("\n") + 1 );
        if(result.endsWith("```"))
            result = result.substring( 0, result.length()-3);
        return result;
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
//                    result.replace(code, "");// remove prompt (if any
//                    if(result.startsWith("```"+code))
//                        result = result.substring(0,code.length()+3);
//                    if(result.startsWith(code))
//                        result = result.substring(0,code.length()+3);
//                    result = extractCode(result); // ```<code>``` -> <code>
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
