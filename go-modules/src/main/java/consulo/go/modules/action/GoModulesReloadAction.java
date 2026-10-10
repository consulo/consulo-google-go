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
import consulo.application.dumb.DumbAware;
import consulo.document.FileDocumentManager;
import consulo.go.localize.GoLocalize;
import consulo.go.modules.GoModulesProjects;
import consulo.go.modules.externalSystem.GoModulesProjectLinker;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.AnActionWithAsyncUpdate;
import consulo.util.concurrent.coroutine.Coroutine;
import consulo.virtualFileSystem.VirtualFile;

/**
 * Re-reads every linked Go modules project with the {@code go} command; links the project in context when there is none yet.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
@ActionImpl(id = "Go.Modules.Reload")
public class GoModulesReloadAction extends AnAction implements DumbAware, AnActionWithAsyncUpdate {
    public GoModulesReloadAction() {
        super(GoLocalize.goModulesActionReloadText(), GoLocalize.goModulesActionReloadDescription(), PlatformIconGroup.actionsRefresh());
    }

    @Override
    public Coroutine<?, ?> updateAsync(AnActionEvent e) {
        return GoModulesActions.updateVisibility(e, project ->
            GoModulesProjects.hasLinkedProjects(project) || GoModulesActions.findProjectRootToLink(e, project) != null);
    }

    @RequiredUIAccess
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(Project.KEY);
        if (project == null) {
            return;
        }
        FileDocumentManager.getInstance().saveAllDocuments();
        if (GoModulesProjects.hasLinkedProjects(project)) {
            GoModulesProjectLinker.refreshAll(project);
            return;
        }
        VirtualFile root = GoModulesActions.findProjectRootToLink(e, project);
        if (root != null) {
            GoModulesProjectLinker.link(project, root.getPath(), null);
        }
    }
}
