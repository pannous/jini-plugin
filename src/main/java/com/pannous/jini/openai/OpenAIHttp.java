package com.pannous.jini.openai;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.pannous.jini.settings.AppSettingsState;
import com.pannous.jini.settings.Options;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.pannous.jini.openai.OpenAITools.extractCode;
import static com.pannous.jini.openai.OpenAITools.getKey;
import static com.pannous.jini.settings.Options.replace;

public class OpenAIHttp {


    public static void query(Project project, Prompt prompt, String code, String language, Consumer<String> callback, Options options) {
        if (code.length() > 3000) code = code.substring(0, 3000); // todo split / notify
        AppSettingsState settings = AppSettingsState.getInstance();
        if (settings != null && (settings.OPENAI_API_KEY == null || settings.OPENAI_API_KEY.isEmpty())) {
            getKey(project);
            return;
        }

        String safe_code = code.replaceAll("\"", "'").replace("\n", "\\n");
        String safe_prompt = prompt.getText().replaceAll("\"", "'");

        String json = "[";
        json += "{\"role\":\"system\",\"content\":\"" + safe_prompt + "\"},";
        if (prompt == Prompt.CONVERT)
            json += "{\"role\":\"system\",\"content\":\"Target language: " + prompt.language + "\"},";
        json += "{\"role\":\"user\",\"content\":\"" + safe_code + "\"},";
        if (options.has(replace) || options.has(Options.noExplanations)) {
            json += "{\"role\":\"system\",\"content\":\"DO NOT OUTPUT ANY COMMENTS OR EXPLANATIONS\"},";
        }
        if (prompt == Prompt.EXECUTE) {
            json += "{\"role\":\"system\",\"content\":\"OS: " + System.getProperty("os.name") + "\"},";
            json += "{\"role\":\"system\",\"content\":\"ARCH: " + System.getProperty("os.arch") + "\"},";
        } else {
            json += "{\"role\":\"system\",\"content\":\"programming language: " + language + "\"},";
        }
        if (json.endsWith(","))
            json = json.substring(0, json.length() - 1);// fuck json trailing comma!!!
        json += "]";
        fetchInBackground(callback, prompt.name(), json, project, code);
    }


    public static void fetchInBackground(Consumer<String> callback, String info, String json, Project project, String code) {
        String openAiKey = getKey(project);
        Task.Backgroundable task = new Task.Backgroundable(project, "Get GPT answer", true) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                progressIndicator.setIndeterminate(true);
                progressIndicator.setText(info);
                if (!progressIndicator.isRunning())
                    progressIndicator.start();
                try {// Perform long-running operation
                    OpenAiAPI api = new OpenAiAPI();
                    String result = api.query(json);
                    progressIndicator.stop();
                    result = extractCode(result); // ```<code>``` -> <code>
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
