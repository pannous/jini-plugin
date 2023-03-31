package com.pannous.jini;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.merge.MergeData;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Arrays;
import java.util.stream.Stream;

public class Merger {

    public static void showMerger(Project project, Editor editor, int selectionStart, int selectionEnd, String finalText) {
        // Create MergeData instance
        MergeData mergeData = new MergeData();
        Document document = editor.getDocument();
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        assert file != null;

        // Replace the selected text
        Document clonedDocument = EditorFactory.getInstance().createDocument(document.getText());
        clonedDocument.replaceString(selectionStart, selectionEnd, finalText);

        DiffContent beforeContent = DiffContentFactory.getInstance().create(project, file);
        DiffContent afterContent = DiffContentFactory.getInstance().create(project, clonedDocument);
        DiffRequest diffRequest = new SimpleDiffRequest("Before and After", beforeContent, afterContent, "Before", "After");
        DiffManager diffManager = DiffManager.getInstance();
//        diffManager.showMerge(project, diffRequest, mergeData);
//        @NotNull DiffDialogHints hints=DiffDialogHints.NON_MODAL;
//        diffManager.showDiff(project, diffRequest,hints);
        diffManager.showDiff(project, diffRequest);

//    CheckboxDiffTool tool = new CheckboxDiffTool(request);
//    tool.show();
    }

}
