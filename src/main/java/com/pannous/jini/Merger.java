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
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.vcs.merge.MergeData;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Arrays;
import java.util.stream.Stream;

public class Merger {


    public static MergeData loadMergeData() {
        Trinity<String, String, String> blobs;
        MergeData mergeData = new MergeData();
//
//    mergeData.ORIGINAL = originalContent;
//    mergeData.CURRENT = !isReversed ? yoursContent : theirsContent;
//    mergeData.LAST = isReversed ? yoursContent : theirsContent;
//
//    mergeData.ORIGINAL_REVISION_NUMBER = originalRevision;
//    mergeData.CURRENT_REVISION_NUMBER = !isReversed ? yoursRevision : theirsRevision;
//    mergeData.LAST_REVISION_NUMBER = isReversed ? yoursRevision : theirsRevision;
//
//    mergeData.ORIGINAL_FILE_PATH = originalPath;
//    mergeData.CURRENT_FILE_PATH = !isReversed ? yoursPath : theirsPath;
//    mergeData.LAST_FILE_PATH = isReversed ? yoursPath : theirsPath;

        // THREE WAY MERGE!
//    val byteContents = listOf(mergeData.CURRENT, mergeData.ORIGINAL, mergeData.LAST)
//    val contents = DiffUtil.getDocumentContentsForViewer(project, byteContents, filePath, mergeData.CONFLICT_TYPE)
//    val request = SimpleDiffRequest(title, contents.toList(), titles)
//    putRevisionInfos(request, mergeData)
        return mergeData;
    }

    public static void showMerger(Project project, Editor editor, int selectionStart, int selectionEnd, String finalText) {
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
