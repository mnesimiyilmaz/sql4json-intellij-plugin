package io.github.mnesimiyilmaz.sql4jsonintellijplugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.json.JsonLanguage;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.github.mnesimiyilmaz.sql4json.SQL4JsonInput;
import io.github.mnesimiyilmaz.sql4json.SQL4JsonProcessor;
import io.github.mnesimiyilmaz.sql4json.utils.JsonUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Optional;

public class SQL4JsonWindowContent extends SimpleToolWindowPanel implements Disposable {

    private final transient Project             project;
    private final transient Editor              resultEditor;
    private final transient Editor              queryEditor;
    private final transient FileEditorManager   fileEditorManager;
    private final transient FileDocumentManager fileDocumentManager;
    private final           JButton             executeButton;

    private boolean disposed;

    public SQL4JsonWindowContent(@NotNull Project project) {
        super(false, false);
        this.setLayout(new BorderLayout(0, 5));

        this.project = project;
        this.fileEditorManager = FileEditorManager.getInstance(project);
        this.fileDocumentManager = FileDocumentManager.getInstance();
        this.queryEditor = createTextEditor("SQL Query");
        this.resultEditor = createTextEditor("Results");
        this.executeButton = new JButton("Execute");
        executeButton.addActionListener(onExecuteButtonClick());

        JPanel queryPanel = new JPanel(new BorderLayout());
        queryPanel.add(queryEditor.getComponent(), BorderLayout.CENTER);
        queryPanel.add(executeButton, BorderLayout.EAST);
        this.add(queryPanel, BorderLayout.NORTH);

        JPanel resultPanel = new JPanel(new BorderLayout(0, 3));
        resultPanel.add(resultEditor.getComponent(), BorderLayout.CENTER);
        this.add(resultPanel, BorderLayout.CENTER);
    }

    @Override
    public void dispose() {
        if (disposed) return;
        this.disposed = true;
        if (resultEditor != null) {
            EditorFactory.getInstance().releaseEditor(resultEditor);
        }
        if (queryEditor != null) {
            EditorFactory.getInstance().releaseEditor(queryEditor);
        }
    }

    private ActionListener onExecuteButtonClick() {
        return event -> {
            Optional<Document> document = getCurrentyOpenJsonFile();
            if (document.isPresent()) {
                executeButton.setEnabled(false);
                try {
                    String result = getExecuteQueryAndGetResult(document.get());
                    WriteCommandAction.runWriteCommandAction(
                            project, () -> resultEditor.getDocument().setText(result));
                } catch (Exception ex) {
                    WriteCommandAction.runWriteCommandAction(
                            project, () -> resultEditor.getDocument().setText(ex.getMessage()));
                } finally {
                    executeButton.setEnabled(true);
                }
            }
        };
    }

    private Optional<Document> getCurrentyOpenJsonFile() {
        FileEditor[] fileEditors = fileEditorManager.getSelectedEditors();
        if (fileEditors.length > 0) {
            VirtualFile currentFile = fileEditors[0].getFile();
            if (currentFile != null && !currentFile.isDirectory() &&
                    currentFile.getExtension() != null && currentFile.getExtension().equals("json")) {
                return Optional.ofNullable(fileDocumentManager.getDocument(currentFile));
            }
        }
        WriteCommandAction.runWriteCommandAction(
                project, () -> resultEditor.getDocument().setText("Please open a json file"));
        return Optional.empty();
    }

    @NotNull
    private String getExecuteQueryAndGetResult(Document jsonFile) throws JsonProcessingException {
        String query = queryEditor.getDocument().getText();
        if (query.isEmpty() || query.isBlank()) {
            return "Please provide a query";
        }
        SQL4JsonInput sql4JsonInput = SQL4JsonInput.fromJsonString(queryEditor.getDocument().getText(), jsonFile.getText());
        SQL4JsonProcessor processor = new SQL4JsonProcessor(sql4JsonInput);
        JsonNode result = processor.getResult();
        String jsonResultString = JsonUtils.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result);
        return StringUtil.convertLineSeparators(jsonResultString);
    }

    private Editor createTextEditor(String placeholder) {
        Document document = EditorFactory.getInstance().createDocument("");
        EditorEx editor = (EditorEx) EditorFactory.getInstance().createEditor(document, this.project, EditorKind.CONSOLE);
        editor.getSettings().setLanguageSupplier(() -> JsonLanguage.INSTANCE);
        editor.setPlaceholder(placeholder);
        editor.setShowPlaceholderWhenFocused(true);
        EditorSettings settings = editor.getSettings();
        settings.setUseSoftWraps(true);
        settings.setAdditionalLinesCount(0);
        settings.setAdditionalColumnsCount(1);
        settings.setWheelFontChangeEnabled(false);
        settings.setAdditionalPageAtBottom(false);
        settings.setCaretRowShown(false);
        return editor;
    }

}
