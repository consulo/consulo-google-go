/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.go.modules.action;

import consulo.annotation.component.ActionImpl;
import consulo.annotation.component.ActionParentRef;
import consulo.annotation.component.ActionRef;
import consulo.annotation.component.ActionRefAnchor;
import consulo.application.dumb.DumbAware;
import consulo.go.icon.GoIconGroup;
import consulo.go.localize.GoLocalize;
import consulo.go.modules.GoModulesProjects;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.AnActionWithAsyncUpdate;
import consulo.ui.ex.action.AnSeparator;
import consulo.ui.ex.action.DefaultActionGroup;
import consulo.util.concurrent.coroutine.Coroutine;
import consulo.virtualFileSystem.VirtualFile;

/**
 * The Go Modules menu, in the Tools menu and in the popups of files and modules that belong to a Go module.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
@ActionImpl(id = "Go.Modules", children = {
    @ActionRef(type = GoModulesReloadAction.class),
    @ActionRef(type = GoModulesLinkAction.class),
    @ActionRef(type = AnSeparator.class),
    @ActionRef(type = GoModulesBuildAction.class),
    @ActionRef(type = GoModulesTestAction.class),
    @ActionRef(type = GoModulesVetAction.class),
    @ActionRef(type = GoModulesGenerateAction.class),
    @ActionRef(type = GoModulesInstallAction.class),
    @ActionRef(type = AnSeparator.class),
    @ActionRef(type = GoModTidyAction.class),
    @ActionRef(type = GoModDownloadAction.class),
    @ActionRef(type = GoModVendorAction.class),
    @ActionRef(type = GoModVerifyAction.class),
    @ActionRef(type = GoWorkSyncAction.class),
}, parents = {
    @ActionParentRef(value = @ActionRef(id = "ToolsMenu"), anchor = ActionRefAnchor.LAST),
    @ActionParentRef(@ActionRef(id = "ProjectViewPopupMenu")),
    @ActionParentRef(@ActionRef(id = "EditorPopupMenu"))
})
public class GoModulesActionGroup extends DefaultActionGroup implements DumbAware, AnActionWithAsyncUpdate {
    public GoModulesActionGroup() {
        super(GoLocalize.goModulesActionGroupText(), GoLocalize.goModulesActionGroupDescription(), GoIconGroup.gomod());
        setPopup(true);
    }

    @Override
    public Coroutine<?, ?> updateAsync(AnActionEvent e) {
        return GoModulesActions.updateVisibility(e, project -> {
            VirtualFile file = e.getData(VirtualFile.KEY);
            if (file != null && file.isInLocalFileSystem()) {
                return GoModulesProjects.isProjectFile(project, file) && GoModulesProjects.findProjectRoot(file) != null;
            }
            return GoModulesProjects.hasLinkedProjects(project) || GoModulesActions.findProjectRootToLink(e, project) != null;
        });
    }
}
