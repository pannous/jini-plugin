package com.pannous.jini;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.pannous.jini.openai.Prompt;
import com.pannous.jini.settings.Options;
import org.jetbrains.annotations.NotNull;

public class Tests extends Action {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        performAction(e, Prompt.TESTS,  Options.newFile.or(Options.insert_before));
    }
}
