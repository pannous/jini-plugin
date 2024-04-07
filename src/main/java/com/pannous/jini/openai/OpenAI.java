package com.pannous.jini.openai;

import com.intellij.openapi.project.Project;
import com.pannous.jini.settings.Options;

import java.util.function.Consumer;

public class OpenAI {


    public static void query(Project project, Prompt prompt, String code, String language, Consumer<String> callback, Options options) {
//    OpenAIHttp.query(project, prompt, code, language, callback, options);  // OWN
//    OpenAiTheo.query(project, prompt, code, language, callback, options);
        OpenAiCj.query(project, prompt, code, language, callback, options);
    }


}
