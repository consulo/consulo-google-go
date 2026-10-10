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

package consulo.go.modules;

import consulo.go.modules.module.GoModulesModuleExtension;
import consulo.go.modules.setting.GoModulesProjectSettings;
import consulo.go.modules.setting.GoModulesSettings;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ProjectFileIndex;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Finds Go modules projects on disk the way the {@code go} command does: a {@code go.work} above the directory wins over
 * the nearest {@code go.mod}.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public final class GoModulesProjects {
    private GoModulesProjects() {
    }

    /**
     * @return the directory a linked project path stands for; a path to a {@code go.mod} or {@code go.work} means its directory
     */
    public static Path toDirectory(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        Path fileName = normalized.getFileName();
        if (fileName != null && GoModulesConstants.SETTINGS_FILE_NAMES.contains(fileName.toString()) && !Files.isDirectory(normalized)) {
            Path parent = normalized.getParent();
            return parent != null ? parent : normalized;
        }
        return normalized;
    }

    /**
     * @return the directory to link for {@code fileOrDirectory}: the one of the {@code go.work} in effect, else of the
     * nearest {@code go.mod}; {@code null} when the file is outside any Go module
     */
    public static @Nullable VirtualFile findProjectRoot(VirtualFile fileOrDirectory) {
        VirtualFile moduleRoot = null;
        VirtualFile workRoot = null;
        for (VirtualFile directory = fileOrDirectory.isDirectory() ? fileOrDirectory : fileOrDirectory.getParent();
             directory != null;
             directory = directory.getParent()) {
            if (moduleRoot == null && isFile(directory.findChild(GoModulesConstants.GO_MOD))) {
                moduleRoot = directory;
            }
            if (isFile(directory.findChild(GoModulesConstants.GO_WORK))) {
                workRoot = directory;
                break;
            }
        }
        return workRoot != null ? workRoot : moduleRoot;
    }

    /**
     * @return the directory of the nearest {@code go.mod} above {@code fileOrDirectory}
     */
    public static @Nullable VirtualFile findModuleRoot(VirtualFile fileOrDirectory) {
        for (VirtualFile directory = fileOrDirectory.isDirectory() ? fileOrDirectory : fileOrDirectory.getParent();
             directory != null;
             directory = directory.getParent()) {
            if (isFile(directory.findChild(GoModulesConstants.GO_MOD))) {
                return directory;
            }
        }
        return null;
    }

    /**
     * @return whether {@code file} is a file of the user, not of a dependency in the module cache or of the SDK:
     * those have a {@code go.mod} of their own but are never linked
     */
    public static boolean isProjectFile(Project project, VirtualFile file) {
        return file.isInLocalFileSystem() && !ProjectFileIndex.getInstance(project).isInLibrary(file);
    }

    public static boolean isSettingsFile(VirtualFile file) {
        return !file.isDirectory() && GoModulesConstants.SETTINGS_FILE_NAMES.contains(file.getName());
    }

    public static boolean hasLinkedProjects(Project project) {
        return !project.isDefault() && !GoModulesSettings.getInstance(project).getLinkedProjectsSettings().isEmpty();
    }

    /**
     * @return whether {@code directory} is a linked project or one of the modules a linked project imported
     */
    public static boolean isLinked(Project project, VirtualFile directory) {
        if (project.isDefault()) {
            return false;
        }
        if (GoModulesSettings.getInstance(project).findLinkedProjectSettingsFor(directory.getPath()) != null) {
            return true;
        }
        Path path = Path.of(directory.getPath());
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            GoModulesModuleExtension extension = GoModulesModuleExtension.find(module);
            if (extension != null && Objects.equals(extension.getModuleDirectory(), path)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getLinkedProjectPaths(Project project) {
        List<String> paths = new ArrayList<>();
        if (project.isDefault()) {
            return paths;
        }
        for (GoModulesProjectSettings settings : GoModulesSettings.getInstance(project).getLinkedProjectsSettings()) {
            String path = settings.getExternalProjectPath();
            if (path != null) {
                paths.add(path);
            }
        }
        return paths;
    }

    /**
     * @return the modules imported from the linked project at {@code linkedProjectPath}
     */
    public static List<Module> getImportedModules(Project project, String linkedProjectPath) {
        List<Module> modules = new ArrayList<>();
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            GoModulesModuleExtension extension = GoModulesModuleExtension.find(module);
            if (extension != null && linkedProjectPath.equals(extension.getRootProjectPath())) {
                modules.add(module);
            }
        }
        return modules;
    }

    private static boolean isFile(@Nullable VirtualFile file) {
        return file != null && !file.isDirectory();
    }
}
