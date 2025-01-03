package com.goide.actions.tool;

import consulo.annotation.component.ActionImpl;
import consulo.annotation.component.ActionParentRef;
import consulo.annotation.component.ActionRef;
import consulo.application.dumb.DumbAware;
import consulo.google.go.icon.GoogleGoIconGroup;
import consulo.google.go.module.extension.GoModuleExtension;
import consulo.localize.LocalizeValue;
import consulo.module.extension.ModuleExtensionHelper;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DefaultActionGroup;
import consulo.ui.ex.action.Presentation;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2025-01-03
 */
@ActionImpl(id = "GoTools", children = {
    @ActionRef(type = GoFmtFileAction.class),
    @ActionRef(type = GoFmtProjectAction.class),
    @ActionRef(type = GoImportsFileAction.class),
    @ActionRef(type = GoTypeFileAction.class),
    @ActionRef(type = GoVetFileAction.class),
}, parents = {
    @ActionParentRef(@ActionRef(id = "CodeMenu")),
    @ActionParentRef(@ActionRef(id = "ProjectViewPopupMenu")),
    @ActionParentRef(@ActionRef(id = "EditorLangPopupMenu"))
})
public class GoExternalToolsActionGroup extends DefaultActionGroup implements DumbAware {
    public GoExternalToolsActionGroup() {
        super(LocalizeValue.localizeTODO("Go Tools"), LocalizeValue.localizeTODO("Go External Tools"), GoogleGoIconGroup.go());
    }

    @Override
    public boolean isPopup() {
        return true;
    }

    @RequiredUIAccess
    @Override
    public void update(@Nonnull AnActionEvent e) {
        Project project = e.getData(Project.KEY);
        Presentation presentation = e.getPresentation();

        boolean enabled = false;
        if (project != null) {
            enabled = ModuleExtensionHelper.getInstance(project).hasModuleExtension(GoModuleExtension.class);
        }

        presentation.setEnabledAndVisible(enabled);
    }
}
