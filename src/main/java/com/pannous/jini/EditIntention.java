
package com.pannous.jini;

import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.pannous.jini.openai.OpenAI2;
import com.pannous.jini.openai.Prompt;
import com.pannous.jini.settings.AppSettingsState;
import com.pannous.jini.settings.Options;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.pannous.jini.Util.*;
import static com.pannous.jini.settings.Options.replace;

// todo: very similar to Refactor -> Custom AI Refactor ⌘ + ⇧ + E  // ⌥ + ⏎
// AI Modify / Change / Edit / Refactor via Smart Command Intentions
public class EditIntention extends BaseElementAtCaretIntentionAction {

    private String command;

    @Override
    public @NotNull String getText() {
        return "AI Modify";
    }

    @Override
    public @NotNull String getFamilyName() {
        return "Jini AI fixes";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return true;
    }

    //  It's not allowed to modify any physical PSI or spawn any actions in other threads within this method.
    @Override
    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        // file is a copy of the original file with line(s) to be modified, diff shown in preview
        System.err.println("FixIntention.generatePreview");
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = file.findElementAt(offset);
        String text = elementAt.getText();
        elementAt.delete();// nice works when element is selected => gray = delete
        return IntentionPreviewInfo.DIFF;
//        System.errr.println("FixIntention.generatePreview");
//        return IntentionAction.super.generatePreview(project, editor, file);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        System.err.println("EditIntention.invoke");
        Caret caret = editor.getCaretModel().getCurrentCaret();
        Language language = element.getLanguage();
        String errorLine = element.getText();
//         Exceptions occurred on invoking the intention 'AI fix' on a copy of the file.
// IntentionPreviewUnsupportedOperationException: It's unexpected to invoke this method on an intention preview calculating.

        String code = caret.getSelectedText();
        if (code == null || code.trim().isEmpty())
            code = getCurrentLine(editor);

        AppSettingsState settings = AppSettingsState.getInstance();
        if (this.command == null || this.command.isEmpty())
            this.command = settings.customRefactor;
        // NOT ALLOWED
//        java.lang.Throwable: AWT events are not allowed inside write action:
        String finalCode = code;

//        element.delete(); // works but not what we want yet!
//        element.delete();// NOT HERE! remove redundant code. last time we're allowed to do this

        ApplicationManager.getApplication().invokeLater(() -> {
            String userInput = Messages.showInputDialog(
                    "How to modify the selected code",
                    "Instructions",
                    Messages.getQuestionIcon(),
                    this.command,
                    null);

            if (userInput == null) return;
            command = userInput; // 🤍
            String message = "CODE: " + finalCode + "\n\nCOMMAND: " + command;
            Prompt prompt = Prompt.EDIT;
            Consumer<String> callback;
            updateToolWindow(message, project);
            callback = (result) -> {
                writeResult(project, editor, caret, result, prompt, Options.replace);
                updateToolWindow(result, project);
            };
            OpenAI2.query(project, prompt, message, language.getDisplayName(), callback, replace);
        });

    }


    @Override
    public boolean startInWriteAction() {
        return true;
    }

}
