package com.pannous.jini;

import com.intellij.history.integration.ui.actions.ShowHistoryAction;
import com.intellij.lang.Commenter;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageCommenters;
import com.intellij.openapi.actionSystem.ActionManager;
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
import com.pannous.jini.openai.OpenAI2;
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

import static com.pannous.jini.Util.*;
import static com.pannous.jini.openai.OpenAITools.extractInlineCode;
import static com.pannous.jini.settings.Options.replace;


// Hi

public abstract class Action extends AnAction {


    private static void showDiff(@NotNull AnActionEvent event) {
        // Open the history for the current file
        ActionManager actionManager = ActionManager.getInstance();
        ShowHistoryAction showHistoryAction = (ShowHistoryAction) actionManager.getAction("ShowHistory");
        showHistoryAction.actionPerformed(event);
    }





    private void writeFile(Project project, VirtualFile file, String result, Prompt prompt) {
        VirtualFile currentFile = file;
        VirtualFile currentDirectory = currentFile.getParent();
        String newFileName;
        if (prompt.getText().contains(Prompt.CONVERT.getText())) {
            newFileName = file.getNameWithoutExtension() + "." + prompt.language;
        } else if (prompt == Prompt.TESTS) {
            newFileName = file.getNameWithoutExtension() + "Test." + file.getExtension();
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
        if (selectedText == null || selectedText.isEmpty()) {
            try {
                if (editor != null)
                    selectedText = editor.getDocument().getText();
                else if (file != null)
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
        OpenAI2.query(project, prompt, selectedText, language.getDisplayName(), callback, replace);
    }


}
