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
import consulo.go.localize.GoLocalize;
import consulo.go.modules.externalSystem.GoModulesTasks;
import consulo.project.Project;
import consulo.ui.ex.action.AnActionEvent;
import consulo.virtualFileSystem.VirtualFile;

import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
@ActionImpl(id = "Go.Modules.WorkSync")
public class GoWorkSyncAction extends GoModulesTaskAction {
    public GoWorkSyncAction() {
        super(GoModulesTasks.WORK_SYNC, true, GoLocalize.goModulesActionWorkSyncText(), GoLocalize.goModulesActionWorkSyncDescription(), null);
    }

    @Override
    protected List<VirtualFile> findDirectories(AnActionEvent e, Project project) {
        return GoModulesActions.findWorkspaceDirectories(e, project);
    }
}
