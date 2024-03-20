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

public class OpenAITools {

    static String getKey(@NotNull Project project) {
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
        result = result.replaceAll("\\\"", "\"");
        return result;
    }

}
