package com.pannous.jini;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.pannous.jini.openai.Prompt;
import com.pannous.jini.settings.Options;
import org.jetbrains.annotations.NotNull;

// This class is used to convert the code to a different language.
public class Transpile extends Action {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String userInput = Messages.showInputDialog("Transpile to code to the following language", "Convert", Messages.getQuestionIcon());
        if (userInput == null) return;
        Prompt convert = Prompt.CONVERT;
        convert.language = userInput;
        performAction(e, convert, Options.newFile.or(Options.replace));
    }
}
