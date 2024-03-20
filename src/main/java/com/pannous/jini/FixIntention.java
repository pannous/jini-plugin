
package com.pannous.jini;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.IncorrectOperationException;
import com.pannous.jini.openai.OpenAI2;
import com.pannous.jini.openai.Prompt;
import com.pannous.jini.settings.Options;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//import com.intellij.codeInsight.intention.CommonIntentionAction;

import java.util.function.Consumer;

import static com.pannous.jini.Util.getCurrentLine;
import static com.pannous.jini.Util.updateToolWindow;
import static com.pannous.jini.Util.writeResult;
import static com.pannous.jini.settings.Options.replace;

public class FixIntention extends BaseElementAtCaretIntentionAction {
//      } implements IntentionAction {

    @Override
    public @NotNull String getText() {
        return "AI fix";
    }

    @Override
    public @NotNull String getFamilyName() {
        return "Jini AI fixes";
    }
//
//    @Override
//    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
//        return true;
//    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return true;
    }

//    @Override public boolean isShowPreview() {
//        return false;
//    }
//    com.intellij.diagnostic.PluginException: Intention Description Dir URL is null: Jini AI fixes; FixIntention; while looking for description.html [Plugin: com.pannous.jini-plugin]


    //     It's not allowed to modify any physical PSI or spawn any actions in other threads within this method.
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

//    @Override
//    public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
//        PsiElement element = file.getOriginalElement();

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        System.err.println("FixIntention.invoke");
        Caret caret = editor.getCaretModel().getCurrentCaret();
        Language language = element.getLanguage();
        String errorLine = element.getText();
//         Exceptions occurred on invoking the intention 'AI fix' on a copy of the file.
// IntentionPreviewUnsupportedOperationException: It's unexpected to invoke this method on an intention preview calculating.

        String code = caret.getSelectedText();
        if (code == null || code.trim().isEmpty())
            code = getCurrentLine(editor);

        element.delete(); // works

        String message = " FIX: " + code;
        Prompt prompt = Prompt.FIX;
        Consumer<String> callback;
        updateToolWindow(message, project);
        callback = (result) -> {
            ApplicationManager.getApplication().invokeLater(() -> {
//                com.intellij.util.IncorrectOperationException: Must not change PSI outside command or undo-transparent action.
//                        element.delete();
            });
//            com.intellij.openapi.diagnostic.RuntimeExceptionWithAttachments: Read access is allowed from inside read-action or Event Dispatch Thread (EDT) only (see Application.runReadAction()); see https://jb.gg/ij-platform-threading for details

            updateToolWindow(result, project);
//            writeResult(project, editor, caret, result, prompt, Options.replace);
        };
        OpenAI2.query(project, prompt, message, language.getDisplayName(), callback, replace);
    }


    @Override
    public boolean startInWriteAction() {
        return true;
    }

//    @Override
//    public void actionPerformed(@NotNull AnActionEvent e) {
//        System.err.println("FixIntention.actionPerformed");
////        performAction(e, Prompt.FIX, Options.replace);
//    }

//    @Override
//    public void actionPerformed(@NotNull AnActionEvent e) {
//// Not called directly
//    }
}
