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

package consulo.go.modules.externalSystem;

import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.ExternalSystemDataKeys;
import consulo.externalSystem.model.task.ProgressExecutionMode;
import consulo.externalSystem.service.project.ExternalProjectRefreshCallback;
import consulo.externalSystem.service.project.ExternalSystemProjectRefresher;
import consulo.externalSystem.service.project.ProjectData;
import consulo.externalSystem.service.project.manage.ProjectDataManager;
import consulo.externalSystem.util.DisposeAwareProjectChange;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.go.modules.GoModulesConstants;
import consulo.go.modules.GoModulesProjects;
import consulo.go.modules.setting.GoModulesProjectSettings;
import consulo.go.modules.setting.GoModulesSettings;
import consulo.module.content.ProjectRootManager;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collections;

/**
 * Links a Go modules project to an IDE project and imports it.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public final class GoModulesProjectLinker {
    private GoModulesProjectLinker() {
    }

    /**
     * @param rootPath a directory with a {@code go.work} or {@code go.mod}, or one of those files
     */
    @RequiredUIAccess
    public static void link(Project project, String rootPath, @Nullable String sdkName) {
        if (project.isDisposed() || project.isDefault()) {
            return;
        }
        String path = ExternalSystemApiUtil.toCanonicalPath(GoModulesProjects.toDirectory(Path.of(rootPath)).toString());
        GoModulesSettings settings = GoModulesSettings.getInstance(project);
        if (settings.getLinkedProjectSettings(path) == null) {
            GoModulesProjectSettings projectSettings = new GoModulesProjectSettings();
            projectSettings.setExternalProjectPath(path);
            projectSettings.setSdkName(sdkName);
            settings.linkProject(projectSettings);
        }
        project.putUserData(ExternalSystemDataKeys.NEWLY_IMPORTED_PROJECT, Boolean.TRUE);
        refresh(project, path);
    }

    @RequiredUIAccess
    public static void refreshAll(Project project) {
        for (String path : GoModulesProjects.getLinkedProjectPaths(project)) {
            refresh(project, path);
        }
    }

    @RequiredUIAccess
    public static void refresh(Project project, String linkedProjectPath) {
        ExternalSystemProjectRefresher.getInstance().refreshProject(project,
            GoModulesConstants.SYSTEM_ID,
            linkedProjectPath,
            new ExternalProjectRefreshCallback() {
                @Override
                public void onSuccess(@Nullable DataNode<ProjectData> externalProject) {
                    if (externalProject != null) {
                        importData(project, externalProject);
                    }
                }

                @Override
                public void onFailure(String errorMessage, @Nullable String errorDetails) {
                }
            },
            false,
            ProgressExecutionMode.IN_BACKGROUND_ASYNC);
    }

    public static void importData(Project project, DataNode<ProjectData> externalProject) {
        ExternalSystemApiUtil.executeProjectChangeAction(true, new DisposeAwareProjectChange(project) {
            @RequiredUIAccess
            @Override
            public void execute() {
                ProjectDataManager projectDataManager = project.getApplication().getInstance(ProjectDataManager.class);
                ProjectRootManager.getInstance(project).mergeRootsChangesDuring(
                    () -> projectDataManager.importData(externalProject.getKey(), Collections.singleton(externalProject), project, true)
                );
            }
        });
    }
}
