package com.pannous.jini;

import com.intellij.lang.Commenter;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageCommenters;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.util.messages.MessageBus;
import com.pannous.jini.openai.OpenAI;
import com.pannous.jini.openai.Prompt;
import com.pannous.jini.settings.AppSettingsState;
import com.pannous.jini.settings.Options;
import com.pannous.jini.window.JiniListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.function.Consumer;

import static com.pannous.jini.openai.OpenAI.extractInlineCode;
import static com.pannous.jini.settings.Options.replace;


// Hi

public abstract class Action extends AnAction {


    private static String formatComment(Language language, String result) {
        Commenter commenter = LanguageCommenters.INSTANCE.forLanguage(language);
        String prefix = commenter.getCommentedBlockCommentPrefix();
        String suffix = commenter.getCommentedBlockCommentSuffix();
        if (prefix == null) {
            prefix = commenter.getLineCommentPrefix();
            result = result.replaceAll("\n", "\n" + prefix);
        }
        if (prefix == null) prefix = "// ";// ⚠️ how?
        if (suffix != null) result = result.replace(suffix, "");
        else suffix = "";
        String lamp = " \uD83D\uDCA1 "; // 💡
        return prefix + lamp + result + suffix + "\n";
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
                if (options.has(replace))
                    editor.getDocument().replaceString(selectionStart, selectionEnd, finalText);
                else if (options.has(Options.insert_before) || settings!=null && settings.autoAddComments)
                    editor.getDocument().insertString(finalOffset, finalText);
            }));
        });
    }

    void updateToolWindow(String result, Project project) {
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


    private static Language getLanguage(Editor editor) {
        if (editor == null) return Language.ANY;
        Document document = editor.getDocument();
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        return getLanguage(file);
    }

    private static Language getLanguage(VirtualFile file) {
        if (file == null) return Language.ANY;
        FileType fileType = file.getFileType();
        if (fileType instanceof LanguageFileType)
            return ((LanguageFileType) fileType).getLanguage();
        return Language.ANY;
    }


    private void writeFile(Project project, VirtualFile file, String result, Prompt prompt) {
        VirtualFile currentFile = file;
        VirtualFile currentDirectory = currentFile.getParent();
        String newFileName;
        if (prompt.getText().contains(Prompt.CONVERT.getText())) {
            newFileName = file.getNameWithoutExtension() + "." + prompt.language;
        } else {
            newFileName = file.getNameWithoutExtension() + "_new." + file.getExtension();
        }
        String newPath = currentDirectory.getPath() + "/" + newFileName;
        // Create a new virtual file
        ApplicationManager.getApplication().invokeLater(() -> {
            ApplicationManager.getApplication().runWriteAction(() -> {
                try {
                    VirtualFile newFile = currentDirectory.createChildData(this, newFileName);
                    FileEditorManager.getInstance(project).openFile(newFile, true);
                    Document document = FileDocumentManager.getInstance().getDocument(newFile);
                    document.setText(result);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        });
    }


    private String getText(VirtualFile file, Editor editor, Caret caret, Options options) {
        String selectedText = caret != null ? caret.getSelectedText() : null;
        if (selectedText == null && caret != null && !options.has(Options.newFile) && !options.has(Options.fix)) {
            // Options.extendToLine
            caret.selectLineAtCaret();
            selectedText = caret.getSelectedText();
        }
//        else{
//            options=options.remove(Options.insert_before);
//        }
        if (selectedText == null || selectedText.isEmpty()) {
            try {
                if (editor != null)
                    selectedText = editor.getDocument().getText();
                else if (file != null)
//                        selectedText = new String(file.getInputStream().readAllBytes(), file.getCharset());
                    selectedText = new String(file.contentsToByteArray(), file.getCharset());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return selectedText;
    }

    public void performAction(@NotNull AnActionEvent event, Prompt prompt, Options options) {
        AppSettingsState settings = AppSettingsState.getInstance();
        final Project project = event.getData(PlatformDataKeys.PROJECT);
        final Editor editor = event.getData(PlatformDataKeys.EDITOR);
        final Caret caret = event.getData(PlatformDataKeys.CARET);
        VirtualFile file = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        Language language = getLanguage(editor);
        if (file != null) language = getLanguage(file);
        String selectedText = getText(file, editor, caret, options);
        boolean noSelection = selectedText == null || selectedText.isEmpty();
        updateToolWindow("PROMPT: " + prompt.getText() + "\n" + selectedText, project);
        Consumer<String> callback;
        if (options.has(Options.newFile) && settings.autoSaveToNewFile && (noSelection || !options.has(replace))) {
            callback = (result) -> {
                updateToolWindow(result, project);
                writeFile(project, file, result, prompt);
            };
        } else
            callback = (result) -> {
                updateToolWindow(result, project);
                writeResult(project, editor, caret, result, prompt, options);
            };
        OpenAI.query(project, prompt, selectedText, language.getDisplayName(), callback, replace);
    }


}
