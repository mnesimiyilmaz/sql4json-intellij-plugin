package io.github.mnesimiyilmaz.sql4jsonintellijplugin;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class SQL4JsonWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setTitle("SQL4Json Query");
        Content content = ContentFactory.getInstance().createContent(
                new SQL4JsonWindowContent(project), "", true);
        toolWindow.getContentManager().addContent(content);
    }

}
