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

package consulo.go.it;

import com.goide.sdk.GoSdkType;
import consulo.application.ReadAction;
import consulo.application.WriteAction;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListenerAdapter;
import consulo.externalSystem.model.task.ExternalSystemTaskType;
import consulo.externalSystem.service.project.ProjectData;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.go.modules.GoCommandRunner;
import consulo.go.modules.GoModulesConstants;
import consulo.go.modules.GoToolchain;
import consulo.go.modules.externalSystem.GoModulesProjectLinker;
import consulo.go.modules.externalSystem.GoModulesProjectResolver;
import consulo.go.modules.orderEntry.GoDependencyEntries;
import consulo.go.modules.orderEntry.GoDependencyOrderEntryModel;
import consulo.go.modules.setting.GoModulesExecutionSettings;
import consulo.go.modules.setting.GoModulesProjectSettings;
import consulo.go.modules.setting.GoModulesSettings;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.project.Project;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lays the fixtures of {@code src/test/resources/fixtures} out on disk and imports them the way a refresh does: the
 * resolver runs the real {@code go} binary, and the data services apply its result to the project.
 */
final class GoModulesFixtures {
    private GoModulesFixtures() {
    }

    static boolean isGoAvailable() {
        return GoToolchain.fromPath() != null;
    }

    static Path copy(String fixture, Path target) throws IOException {
        URL resource = GoModulesFixtures.class.getResource("/fixtures/" + fixture);
        assertThat(resource).as("fixture " + fixture).isNotNull();
        Path source;
        try {
            source = Path.of(resource.toURI());
        }
        catch (URISyntaxException e) {
            throw new IOException(e);
        }
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path path : (Iterable<Path>) paths::iterator) {
                Path destination = target.resolve(source.relativize(path).toString());
                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                }
                else {
                    Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        LocalFileSystem.getInstance().refreshAndFindFileByNioFile(target);
        return target;
    }

    static String link(Project project, Path root) {
        String path = ExternalSystemApiUtil.toCanonicalPath(root.toString());
        GoModulesSettings settings = GoModulesSettings.getInstance(project);
        if (settings.getLinkedProjectSettings(path) == null) {
            GoModulesProjectSettings projectSettings = new GoModulesProjectSettings();
            projectSettings.setExternalProjectPath(path);
            settings.linkProject(projectSettings);
        }
        return path;
    }

    static DataNode<ProjectData> resolve(Project project, String linkedPath) {
        GoModulesExecutionSettings settings = ExternalSystemApiUtil.getExecutionSettings(project, linkedPath, GoModulesConstants.SYSTEM_ID);
        ExternalSystemTaskId id = ExternalSystemTaskId.create(GoModulesConstants.SYSTEM_ID, ExternalSystemTaskType.RESOLVE_PROJECT, project);
        GoModulesProjectResolver resolver = new GoModulesProjectResolver(
            new GoCommandRunner(project.getApplication().getInstance(ProcessHandlerBuilderFactory.class)));
        DataNode<ProjectData> projectNode =
            resolver.resolveProjectInfo(id, linkedPath, false, settings, ExternalSystemTaskNotificationListenerAdapter.NULL_OBJECT);
        assertThat(projectNode).as("the resolver must produce a project for " + linkedPath).isNotNull();
        return projectNode;
    }

    /**
     * Links {@code root}, reads it with {@code go} and imports the result, like a refresh of a linked project.
     */
    static DataNode<ProjectData> importProject(Project project, Path root) {
        String linkedPath = link(project, root);
        DataNode<ProjectData> projectNode = resolve(project, linkedPath);
        GoModulesProjectLinker.importData(project, projectNode);
        return projectNode;
    }

    static Module module(Project project, String name) {
        Module module = ReadAction.compute(() -> ModuleManager.getInstance(project).findModuleByName(name));
        assertThat(module).as("module " + name).isNotNull();
        return module;
    }

    static List<String> moduleNames(Project project) {
        return ReadAction.compute(() -> {
            List<String> names = new ArrayList<>();
            for (Module module : ModuleManager.getInstance(project).getModules()) {
                names.add(module.getName());
            }
            return names;
        });
    }

    static List<GoDependencyOrderEntryModel> dependencies(Module module) {
        return ReadAction.compute(() -> {
            List<GoDependencyOrderEntryModel> models = new ArrayList<>();
            for (OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
                GoDependencyOrderEntryModel model = GoDependencyEntries.getModel(orderEntry);
                if (model != null) {
                    models.add(model);
                }
            }
            return models;
        });
    }

    static @Nullable VirtualFile find(Path path) {
        return LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path);
    }

    static Set<Sdk> goSdks() {
        return new HashSet<>(SdkTable.getInstance().getSdksOfType(GoSdkType.getInstance()));
    }

    /**
     * The import creates a Go SDK for the GOROOT of {@code go} when none matches; the SDK table outlives the project.
     */
    static void removeSdksAddedSince(Set<Sdk> before) {
        for (Sdk sdk : goSdks()) {
            if (!before.contains(sdk)) {
                WriteAction.run(() -> SdkTable.getInstance().removeSdk(sdk));
            }
        }
    }
}
