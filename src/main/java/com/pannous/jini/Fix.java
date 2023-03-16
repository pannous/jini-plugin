package com.pannous.jini;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.pannous.jini.openai.Prompt;
import com.pannous.jini.settings.Options;
import org.jetbrains.annotations.NotNull;

public class Fix extends Action {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        performAction(e, Prompt.FIX, Options.fix);
    }
}
