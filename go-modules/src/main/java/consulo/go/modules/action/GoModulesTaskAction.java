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

import consulo.application.dumb.DumbAware;
import consulo.document.FileDocumentManager;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.AnActionWithAsyncUpdate;
import consulo.ui.image.Image;
import consulo.util.concurrent.coroutine.Coroutine;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Runs one {@code go} task in every Go module the action applies to.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public abstract class GoModulesTaskAction extends AnAction implements DumbAware, AnActionWithAsyncUpdate {
    private final String myTaskName;
    private final boolean myReloadAfter;

    protected GoModulesTaskAction(String taskName, boolean reloadAfter, LocalizeValue text, LocalizeValue description, @Nullable Image icon) {
        super(text, description, icon);
        myTaskName = taskName;
        myReloadAfter = reloadAfter;
    }

    protected List<VirtualFile> findDirectories(AnActionEvent e, Project project) {
        return GoModulesActions.findModuleDirectories(e, project);
    }

    @Override
    public Coroutine<?, ?> updateAsync(AnActionEvent e) {
        return GoModulesActions.updateVisibility(e, project -> !findDirectories(e, project).isEmpty());
    }

    @RequiredUIAccess
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(Project.KEY);
        if (project == null) {
            return;
        }
        FileDocumentManager.getInstance().saveAllDocuments();
        for (VirtualFile directory : findDirectories(e, project)) {
            GoModulesActions.runTask(project, directory.getPath(), myTaskName, myReloadAfter);
        }
    }
}
