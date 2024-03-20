package com.pannous.jini;

import com.intellij.lang.Commenter;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageCommenters;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.util.messages.MessageBus;
import com.pannous.jini.openai.Prompt;
import com.pannous.jini.settings.AppSettingsState;
import com.pannous.jini.settings.Options;
import com.pannous.jini.window.JiniListener;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import static com.pannous.jini.openai.OpenAITools.extractInlineCode;
import static com.pannous.jini.settings.Options.replace;

public class Util {

    public static String getCurrentLine(Editor editor) {
        Document document = editor.getDocument();
        CaretModel caretModel = editor.getCaretModel();
        int offset = caretModel.getOffset();
        int lineNumber = document.getLineNumber(offset);
        int lineStartOffset = document.getLineStartOffset(lineNumber);
        int lineEndOffset = document.getLineEndOffset(lineNumber);

        // Get text of current line
        String lineText = document.getText(new TextRange(lineStartOffset, lineEndOffset));
        return lineText;
    }


    static void updateToolWindow(String result, Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Jini");
                final Content content = toolWindow.getContentManager().getContent(0); // this will give the first tab
                Transferable transferable = new StringSelection(result);
                JComponent component = content.getComponent();
                TransferHandler.TransferSupport data = new TransferHandler.TransferSupport(component, transferable);
                component.getTransferHandler().importData(data);
//                ((MyToolWindow) content.getComponent()).addResponse(result);
            } catch (Exception e) {// fallback
                MessageBus bus = project.getMessageBus();
                bus.syncPublisher(JiniListener.TOPIC).onMessageReceived(result);
            }
        });
    }


    public static void writeResult(Project project, Editor editor, Caret caret, String result, Prompt prompt, Options options) {
        if (result == null || result.isEmpty()) return;

        if (editor == null) return;
        if (caret == null) return;
        AppSettingsState settings = AppSettingsState.getInstance();

        ApplicationManager.getApplication().invokeLater(() -> {
            String text = extractInlineCode(result);
            if (settings == null || settings.autoPopup)
                Messages.showMessageDialog(project, text, "AI Result", Messages.getInformationIcon());
            if (!caret.hasSelection()) {// select line
                caret.setSelection(caret.getVisualLineStart(), caret.getVisualLineEnd());
//                caret.selectLineAtCaret(); // todo before, per case?
                text += "\n"; // ❤️
            }
            int selectionStart = caret.getSelectionStart();
            int offset = selectionStart;
            int selectionEnd = caret.getSelectionEnd();
            if (options.has(replace)) {
                if (settings != null && !settings.autoReplaceCode) return;
            } else {
                if (options.has(Options.insert_after)) {
                    offset = selectionEnd;
                } else {
                    if (options.has(Options.comment) || !options.has(Options.insert_after)) {
                        text = "\n" + formatComment(getLanguage(editor), text);
                    }
                    if (options.has(Options.insert_before)) {
                        caret.selectLineAtCaret();
                        offset = selectionStart - 1;
                        if (offset < 0) offset = 0;
                    }
                }
            }
            int finalOffset = offset;
            String finalText = text;
            ApplicationManager.getApplication().runWriteAction(() -> WriteCommandAction.runWriteCommandAction(project, () -> {
                if (options.has(replace)) {
                    Merger.showMerger(project, editor, selectionStart, selectionEnd, finalText);
//                    editor.getDocument().replaceString(selectionStart, selectionEnd, finalText);
//                    showDiff(event);
                } else if (options.has(Options.insert_before) || settings != null && settings.autoAddComments)
                    editor.getDocument().insertString(finalOffset, finalText);
            }));
        });
    }


    public static Language getLanguage(Editor editor) {
        if (editor == null) return Language.ANY;
        Document document = editor.getDocument();
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        return getLanguage(file);
    }

    public static Language getLanguage(VirtualFile file) {
        if (file == null) return Language.ANY;
        FileType fileType = file.getFileType();
        if (fileType instanceof LanguageFileType)
            return ((LanguageFileType) fileType).getLanguage();
        return Language.ANY;
    }


    public static String formatComment(Language language, String result) {
        Commenter commenter = LanguageCommenters.INSTANCE.forLanguage(language);
        String prefix = "//";
        String suffix = "";
        if (commenter != null) {
            prefix = commenter.getCommentedBlockCommentPrefix();
            suffix = commenter.getCommentedBlockCommentSuffix();
            if (prefix == null) prefix = commenter.getLineCommentPrefix();
            if (prefix == null) prefix = "//";// ⚠️ how?
        }
        result = result.replaceAll("\n", "\n" + prefix);
        if (suffix != null) result = result.replace(suffix, "");
        String lamp = " \uD83D\uDCA1 "; // 💡
        return prefix + lamp + result + suffix + "\n";
    }

}
