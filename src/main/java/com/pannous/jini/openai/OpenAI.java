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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        String safe_code = code.replaceAll("\"", "'").replace("\n", "\\n");
        String safe_prompt = prompt.getText().replaceAll("\"", "'");

        String json = "[";
        if (prompt == Prompt.EXECUTE) {
            json += "{\"role\":\"system\",\"content\":\"OS: " + System.getProperty("os.name") + "\"},";
            json += "{\"role\":\"system\",\"content\":\"ARCH: " + System.getProperty("os.arch") + "\"},";
        } else {
            json += "{\"role\":\"system\",\"content\":\"Language: " + language + "\"},";
        }
        json += "{\"role\":\"system\",\"content\":\"" + safe_prompt + "\"},";
        if (prompt == Prompt.CONVERT)
            json += "{\"role\":\"system\",\"content\":\"Target language: " + prompt.language + "\"},";
        json += "{\"role\":\"user\",\"content\":\"" + safe_code + "\"},";
        if (options.has(replace) || options.has(Options.noExplanations)) {
            json += "{\"role\":\"system\",\"content\":\"DO NOT OUTPUT ANY COMMENTS OR EXPLANATIONS\"},";
        }
        if (json.endsWith(","))
            json = json.substring(0, json.length() - 1);// fuck json trailing comma!!!
        json += "]";
        fetchInBackground(callback, prompt.name(), json, project, code);
    }

    public static String extractCode(String result) {
        if (result.startsWith("```"))
            result = result.substring(result.indexOf("\n") + 1);
        if (result.endsWith("```"))
            result = result.substring(0, result.length() - 3);
        return result;
    }


    public static String extractCodes(String codes) {
        if (!codes.contains("```bash"))
            return extractInlineCode(codes);
        String pattern = "```bash\\s+(.*?)```";
        Pattern p = Pattern.compile(pattern, Pattern.DOTALL);
        Matcher m = p.matcher(codes);
        while (m.find()) {
            String code = m.group(1);
            if (code.isEmpty()) return pattern;
            return code;
        }
        return extractCode(codes);
    }

    // todo extract multiple code blocks from ```<code>```
    public static String extractInlineCode(String result) {
        int i = result.indexOf("```");
        if (i < 0) return result;
        int k = result.indexOf("\n", i + 3);
        result = result.substring(k);
        int j = result.indexOf("```");
        if (j > 0)
            result = result.substring(0, j);
        return result;
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
