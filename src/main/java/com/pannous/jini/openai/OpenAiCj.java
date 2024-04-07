package com.pannous.jini.openai;
import com.cjcrafter.openai.OpenAI;
import com.cjcrafter.openai.chat.ChatMessage;
import com.cjcrafter.openai.chat.ChatRequest;
import com.cjcrafter.openai.chat.ChatResponse;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.pannous.jini.settings.AppSettingsState;
import com.pannous.jini.settings.Options;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import static com.pannous.jini.openai.OpenAITools.getKey;
import static com.pannous.jini.settings.Options.replace;

public class OpenAiCj {
    static OpenAI openai;
  public static void main(String[] args) {

      // To use dotenv, you need to add the "io.github.cdimascio:dotenv-kotlin:version"
      // dependency. Then you can add a .env file in your project directory.
      String token = System.getenv("OPENAI_TOKEN");
      openai = OpenAI.builder()
              .apiKey(token)
              .build();

      List<ChatMessage> messages = new ArrayList<>();
      messages.add(ChatMessage.toSystemMessage("Help the user with their problem."));

      // Here you can change the model's settings, add tools, and more.
      ChatRequest request = ChatRequest.builder()
              .model("gpt-3.5-turbo")
              .messages(messages)
              .build();

      Scanner scan = new Scanner(System.in);
      while (true) {
          System.out.println("What are you having trouble with?");
          String input = scan.nextLine();

          messages.add(ChatMessage.toUserMessage(input));
          ChatResponse response = openai.createChatCompletion(request);

          System.out.println("Generating Response...");
          System.out.println(response.get(0).getMessage().getContent());

          // Make sure to add the response to the messages list!
          messages.add(response.get(0).getMessage());
      }
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
            chatMessages.add(ChatMessage.toSystemMessage( "OS: " + System.getProperty("os.name")));
            chatMessages.add(ChatMessage.toSystemMessage( "ARCH: " + System.getProperty("os.arch")));
        } else {
            chatMessages.add(ChatMessage.toSystemMessage( "Language: " + language));
        }
        chatMessages.add(ChatMessage.toSystemMessage( prompt.getText()));
        if (prompt == Prompt.CONVERT)
            chatMessages.add(ChatMessage.toSystemMessage( "Target language: " + prompt.language));
        chatMessages.add(ChatMessage.toUserMessage( code));
        if (options.has(replace) || options.has(Options.noExplanations)) {
            chatMessages.add(ChatMessage.toSystemMessage( "DO NOT OUTPUT ANY COMMENTS OR EXPLANATIONS"));
        }

        fetchInBackground(callback, prompt.name(), chatMessages, project, code);
    }


    public static void fetchInBackground(Consumer<String> callback, String text, List<ChatMessage> chatMessages, Project project, String code) {
      if(openai==null) {
          String openAiKey = getKey(project);
          openai = OpenAI.builder()
                  .apiKey(openAiKey)
                  .build();
      }
        Task.Backgroundable task = new Task.Backgroundable(project, "Get GPT answer", true) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                progressIndicator.setIndeterminate(true);
                progressIndicator.setText(text);
                if (!progressIndicator.isRunning())
                    progressIndicator.start();
                try {// Perform long-running operation
                    ChatRequest completionRequest = ChatRequest.builder()
                            .model("gpt-3.5-turbo")
                            .messages(chatMessages)
//                            .frequencyPenalty(.5)
//                            .temperature(.2)
                            .maxTokens(3500)
                            .build();
                    ChatResponse response = openai.createChatCompletion(completionRequest);
                    String result = response.getChoices().get(0).getMessage().getContent();
//                    String result = service.createChatCompletion(completionRequest).getChoices().get(0).getMessage().getContent();
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
