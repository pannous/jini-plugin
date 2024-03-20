package com.pannous.jini;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;
import com.pannous.jini.openai.OpenAI2;
import com.pannous.jini.openai.Prompt;
import com.pannous.jini.settings.Options;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static com.pannous.jini.Util.updateToolWindow;
import static com.pannous.jini.Util.writeResult;
import static com.pannous.jini.settings.Options.replace;

public class MyFix extends Action implements LocalQuickFix {
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
        AnActionEvent event = null;
        Prompt prompt = Prompt.FIX;
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
        return "AI fix";
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
