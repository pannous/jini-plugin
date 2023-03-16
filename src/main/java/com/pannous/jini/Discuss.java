package com.pannous.jini;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.pannous.jini.openai.Prompt;
import com.pannous.jini.settings.Options;
import org.jetbrains.annotations.NotNull;

public class Discuss extends Action {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String userInput = Messages.showInputDialog("Question about the selected code, or anything", "Discuss", Messages.getQuestionIcon());
        performAction(e,new Prompt(userInput) , Options.popup);
    }
}
