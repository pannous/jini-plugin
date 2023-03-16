package com.pannous.jini;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.pannous.jini.openai.OpenAI;
import com.pannous.jini.openai.Prompt;
import com.pannous.jini.settings.Options;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Execute extends Action {

    void run(String code, Project project) throws IOException {
        // run code in terminal
        Process process = Runtime.getRuntime().exec(code);
        String result = new String(process.getInputStream().readAllBytes(), "UTF-8"); // proc output => input FOR US!
        ApplicationManager.getApplication().invokeLater(() -> {
            Messages.showMessageDialog(project, result, "result", Messages.getInformationIcon());
        });
    }

//    void run2(String code, Project project) {
//                GeneralCommandLine commandLine = new GeneralCommandLine("/bin/bash");
//                OSProcessHandler processHandler = new OSProcessHandler(commandLine.createProcess(), code);
//                processHandler.addProcessListener(new ProcessAdapter() {
//                    @Override
//                    public void onTextAvailable(@NotNull ProcessEvent processEvent, @NotNull Key key) {
//                        ApplicationManager.getApplication().invokeLater(() -> {
//                            Messages.showMessageDialog(project, processEvent.getText(), "result", Messages.getInformationIcon());
//                        });
//                    }
//                });
//                processHandler.startNotify();
//    }

    void execution(Project project, String codes) {
        try {
            if (codes.isEmpty()) return;
            if (!codes.contains("```")) {
                run(codes, project); // DANGER!
                return;
            }
            String pattern = "```bash\\s+(.*?)```";
            Pattern p = Pattern.compile(pattern, Pattern.DOTALL);
            Matcher m = p.matcher(codes);
            while (m.find()) {
                String code = m.group(1);
                if (code.isEmpty()) return;
                run(code, project);
            }
        } catch (Exception e) {
            ApplicationManager.getApplication().invokeLater(() -> {
                Messages.showMessageDialog(project, e.getMessage(), "Error", Messages.getErrorIcon());
            });
        }

    }

    void confirm(String title, String message, String code, Consumer<String> callback) {
        ApplicationManager.getApplication().invokeLater(() -> {
            int result = Messages.showOkCancelDialog(message, title, " ⚠️ RUN ", " \uD83D\uDED1 ABORT ", Messages.getQuestionIcon());
            if (result == Messages.YES || result == Messages.OK) {
                callback.accept(code);
            }
        });
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        String userInput = Messages.showInputDialog("English command or question to be interpreted as shell script to run in console", "Instructions", Messages.getQuestionIcon());
        if (userInput == null) return;
        final Project project = event.getData(PlatformDataKeys.PROJECT);
        Prompt prompt = Prompt.EXECUTE;
        Consumer<String> doit = (result) -> {
            execution(project, result);
        };
        Consumer<String> doublecheck = (result) -> {
            confirm("Execute shell script", result, result, doit);
        };
        new OpenAI().query(project, prompt, userInput, "bash", doublecheck, Options.none);
    }
}


