package com.pannous.jini;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsSafe;
import com.pannous.jini.openai.Prompt;
import com.pannous.jini.settings.AppSettingsState;
import com.pannous.jini.settings.Options;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


// Custom Refactor Modification
public class Edit extends Action {
    private String command;

    public Edit() {
    }

    public Edit(String userInput) {
        this.command = userInput;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        AppSettingsState settings = AppSettingsState.getInstance();
        if (this.command == null || this.command.isEmpty())
            this.command = settings.customRefactor;
        String userInput = Messages.showInputDialog(
                "How to modify the selected code",
                "Instructions",
                Messages.getQuestionIcon(),
                this.command,
                null);
        if (userInput == null) return;
        this.command = userInput;
        performAction(e, new Prompt(Prompt.EDIT.getText() + userInput), Options.replace);
        settings.customRefactor = userInput;
        ApplicationManager.getApplication().invokeLater(() -> {
            ((PersistentStateComponent<AppSettingsState>) settings).getState();// saves settings!
        });
//        Edit custom_action = new Edit(userInput);
//        ActionManager.getInstance().registerAction(userInput, custom_action);// todo: needs a NAME!
    }
}
