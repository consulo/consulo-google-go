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

import consulo.application.Application;
import consulo.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import consulo.externalSystem.model.task.ProgressExecutionMode;
import consulo.externalSystem.model.task.TaskCallback;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.execution.executor.DefaultRunExecutor;
import consulo.go.modules.GoModulesConstants;
import consulo.go.modules.GoModulesProjects;
import consulo.go.modules.externalSystem.GoModulesProjectLinker;
import consulo.go.modules.module.GoModulesModuleExtension;
import consulo.go.modules.setting.GoModulesProjectSettings;
import consulo.go.modules.setting.GoModulesSettings;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.coroutine.ActionSafeReadLock;
import consulo.util.concurrent.coroutine.Coroutine;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Decides which Go modules an action works on, and runs {@code go} tasks through the external system, so their output
 * lands in the Run tool window like any task started from the Go Modules tool window.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public final class GoModulesActions {
    private GoModulesActions() {
    }

    /**
     * @return the module of the selected file or module, else every module the linked projects imported
     */
    public static List<VirtualFile> findModuleDirectories(AnActionEvent e, Project project) {
        VirtualFile file = e.getData(VirtualFile.KEY);
        if (file != null && file.isInLocalFileSystem()) {
            if (!GoModulesProjects.isProjectFile(project, file)) {
                return List.of();
            }
            VirtualFile moduleRoot = GoModulesProjects.findModuleRoot(file);
            return moduleRoot != null ? List.of(moduleRoot) : List.of();
        }

        Module module = e.getData(Module.KEY);
        if (module != null) {
            VirtualFile directory = moduleDirectory(module);
            return directory != null ? List.of(directory) : List.of();
        }

        Set<VirtualFile> directories = new LinkedHashSet<>();
        for (Module importedModule : ModuleManager.getInstance(project).getModules()) {
            VirtualFile directory = moduleDirectory(importedModule);
            if (directory != null) {
                directories.add(directory);
            }
        }
        return new ArrayList<>(directories);
    }

    /**
     * @return the directories of the {@code go.work} files in context: of the selected file, else of the linked projects
     */
    public static List<VirtualFile> findWorkspaceDirectories(AnActionEvent e, Project project) {
        VirtualFile file = e.getData(VirtualFile.KEY);
        if (file != null && file.isInLocalFileSystem()) {
            if (!GoModulesProjects.isProjectFile(project, file)) {
                return List.of();
            }
            VirtualFile root = GoModulesProjects.findProjectRoot(file);
            return root != null && root.findChild(GoModulesConstants.GO_WORK) != null ? List.of(root) : List.of();
        }

        List<VirtualFile> directories = new ArrayList<>();
        LocalFileSystem fileSystem = LocalFileSystem.getInstance();
        for (String path : GoModulesProjects.getLinkedProjectPaths(project)) {
            VirtualFile directory = fileSystem.findFileByNioFile(Path.of(path));
            if (directory != null && directory.findChild(GoModulesConstants.GO_WORK) != null) {
                directories.add(directory);
            }
        }
        return directories;
    }

    /**
     * @return the directory to link for the selected file, else for the project directory
     */
    public static @Nullable VirtualFile findProjectRootToLink(AnActionEvent e, Project project) {
        VirtualFile file = e.getData(VirtualFile.KEY);
        if (file != null && file.isInLocalFileSystem()) {
            return GoModulesProjects.isProjectFile(project, file) ? GoModulesProjects.findProjectRoot(file) : null;
        }
        VirtualFile baseDir = project.getBaseDir();
        return baseDir != null ? GoModulesProjects.findProjectRoot(baseDir) : null;
    }

    /**
     * Evaluates {@code visible} under a read lock, as it looks at the module model. Action updates must not wait for the
     * lock: when it is taken, the platform leaves the action disabled for this round.
     */
    public static Coroutine<?, ?> updateVisibility(AnActionEvent e, Predicate<Project> visible) {
        return ActionSafeReadLock.<Object, Object>apply(e, presentation -> {
            Project project = e.getData(Project.KEY);
            presentation.setEnabledAndVisible(project != null && !project.isDisposed() && visible.test(project));
            return null;
        }).toCoroutine();
    }

    private static @Nullable VirtualFile moduleDirectory(Module module) {
        GoModulesModuleExtension extension = GoModulesModuleExtension.find(module);
        Path directory = extension != null ? extension.getModuleDirectory() : null;
        return directory != null ? LocalFileSystem.getInstance().findFileByNioFile(directory) : null;
    }

    /**
     * Runs {@code go <taskName>} in {@code directory}.
     *
     * @param reloadAfter whether the linked project is reloaded once the task succeeds, for tasks rewriting {@code go.mod}
     */
    @RequiredUIAccess
    public static void runTask(Project project, String directory, String taskName, boolean reloadAfter) {
        ExternalSystemTaskExecutionSettings settings = new ExternalSystemTaskExecutionSettings();
        settings.setExternalSystemIdString(GoModulesConstants.SYSTEM_ID.getId());
        settings.setExternalProjectPath(directory);
        settings.setTaskNames(List.of(taskName));

        TaskCallback callback = null;
        if (reloadAfter) {
            callback = new TaskCallback() {
                @Override
                public void onSuccess() {
                    Application.get().invokeLater(() -> reload(project, directory));
                }

                @Override
                public void onFailure() {
                }
            };
        }
        ExternalSystemApiUtil.runTask(settings,
            DefaultRunExecutor.EXECUTOR_ID,
            project,
            GoModulesConstants.SYSTEM_ID,
            callback,
            ProgressExecutionMode.IN_BACKGROUND_ASYNC);
    }

    @RequiredUIAccess
    private static void reload(Project project, String directory) {
        if (project.isDisposed()) {
            return;
        }
        GoModulesProjectSettings settings = GoModulesSettings.getInstance(project).findLinkedProjectSettingsFor(directory);
        String linkedPath = settings != null ? settings.getExternalProjectPath() : null;
        if (linkedPath != null) {
            GoModulesProjectLinker.refresh(project, linkedPath);
        }
    }
}
