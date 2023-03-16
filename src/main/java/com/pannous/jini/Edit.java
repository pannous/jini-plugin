package com.pannous.jini;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.ui.Messages;
import com.pannous.jini.openai.Prompt;
import com.pannous.jini.settings.AppSettingsState;
import com.pannous.jini.settings.Options;
import org.jetbrains.annotations.NotNull;


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
        String userInput = command;
        if (userInput == null || userInput.isEmpty()) {
            command = settings.customRefactor;
            userInput = Messages.showInputDialog("How to modify the selected code", "Instructions", Messages.getQuestionIcon(), command, null);
            if (userInput == null) return;
        }
        performAction(e, new Prompt(Prompt.EDIT + userInput), Options.replace);
        settings.customRefactor = userInput;
        ApplicationManager.getApplication().invokeLater(() -> {
            ((PersistentStateComponent<AppSettingsState>) settings).getState();// saves settings!
        });
//        Edit custom_action = new Edit(userInput);
//        ActionManager.getInstance().registerAction(userInput, custom_action);// todo: needs a NAME!
    }
}
