package com.pannous.jini;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;
import com.pannous.jini.openai.OpenAI2;
import com.pannous.jini.openai.Prompt;
import com.pannous.jini.settings.AppSettingsState;
import com.pannous.jini.settings.Options;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static com.pannous.jini.Util.updateToolWindow;
import static com.pannous.jini.Util.writeResult;
import static com.pannous.jini.settings.Options.replace;

// popup PROMPT to Change code
public class Change extends Action implements LocalQuickFix {
    private String command;

    @Override
    public void applyFix(Project project, ProblemDescriptor descriptor) {

        String code = descriptor.getTextRangeInElement().toString();
        PsiElement errorElement = descriptor.getPsiElement();
        String errorLine = errorElement.getText();
        String message = descriptor.getDescriptionTemplate() + errorLine + " CODE: " + code;

        @Nullable VirtualFile file = PsiUtilCore.getVirtualFile(errorElement);
        Language language = PsiUtilCore.getLanguageAtOffset((PsiFile) file, 0);
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        Editor editor = fileEditorManager.getSelectedTextEditor();
        Caret caret = editor.getCaretModel().getCurrentCaret();

        Options options = Options.replace;
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
        Prompt prompt = new Prompt(command);
        Consumer<String> callback;
        callback = (result) -> {
            updateToolWindow(result, project);
            writeResult(project, editor, caret, result, prompt, options);
        };
        OpenAI2.query(project, prompt, message, language.getDisplayName(), callback, replace);

//        errorElement.delete();
    }

    @Override
    public @NotNull String getName() {
        return "AI Change";
    }

    @Override
    public String getFamilyName() {
        return "Jini AI fixes";
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
// Not called directly
    }
}
