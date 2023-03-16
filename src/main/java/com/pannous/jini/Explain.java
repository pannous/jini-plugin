package com.pannous.jini;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.pannous.jini.openai.Prompt;
import com.pannous.jini.settings.Options;
import org.jetbrains.annotations.NotNull;

// Hi

public class Explain extends Action {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        performAction(e, Prompt.EXPLAIN, Options.popup.or(Options.insert_before)  );
    }
}
