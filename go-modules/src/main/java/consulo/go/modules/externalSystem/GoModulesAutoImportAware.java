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

import consulo.application.ReadAction;
import consulo.externalSystem.service.project.autoimport.ExternalSystemAutoImportAware;
import consulo.go.modules.GoModulesConstants;
import consulo.go.modules.GoModulesProjects;
import consulo.go.modules.module.GoModulesModuleExtension;
import consulo.go.modules.setting.GoModulesSettings;
import consulo.module.Module;
import consulo.project.Project;
import org.jspecify.annotations.Nullable;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A linked project depends on its {@code go.work} and on the {@code go.mod} and {@code go.sum} of every imported module.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public class GoModulesAutoImportAware implements ExternalSystemAutoImportAware {
    @Override
    public @Nullable String getAffectedExternalProjectPath(String changedFileOrDirPath, Project project) {
        Path changed;
        try {
            changed = Path.of(changedFileOrDirPath).toAbsolutePath().normalize();
        }
        catch (InvalidPathException e) {
            return null;
        }
        Path fileName = changed.getFileName();
        if (fileName == null || !GoModulesConstants.SETTINGS_FILE_NAMES.contains(fileName.toString())) {
            return null;
        }
        for (String projectPath : GoModulesProjects.getLinkedProjectPaths(project)) {
            if (getAffectedExternalProjectFilePaths(projectPath, project).contains(changed)) {
                return projectPath;
            }
        }
        return null;
    }

    @Override
    public List<Path> getAffectedExternalProjectFilePaths(String projectPath, Project project) {
        Set<Path> files = new LinkedHashSet<>();
        Path root = GoModulesProjects.toDirectory(Path.of(projectPath));
        addSettingsFiles(files, root);
        files.add(root.resolve(GoModulesConstants.GO_WORK));
        files.add(root.resolve(GoModulesConstants.GO_WORK_SUM));

        if (!project.isDefault() && GoModulesSettings.getInstance(project).getLinkedProjectSettings(projectPath) != null) {
            List<Path> moduleDirectories = ReadAction.compute(() -> {
                List<Path> directories = new ArrayList<>();
                for (Module module : GoModulesProjects.getImportedModules(project, projectPath)) {
                    GoModulesModuleExtension extension = GoModulesModuleExtension.find(module);
                    Path directory = extension != null ? extension.getModuleDirectory() : null;
                    if (directory != null) {
                        directories.add(directory);
                    }
                }
                return directories;
            });
            for (Path directory : moduleDirectories) {
                addSettingsFiles(files, directory.toAbsolutePath().normalize());
            }
        }
        return new ArrayList<>(files);
    }

    private static void addSettingsFiles(Set<Path> files, Path directory) {
        files.add(directory.resolve(GoModulesConstants.GO_MOD));
        files.add(directory.resolve(GoModulesConstants.GO_SUM));
    }
}
