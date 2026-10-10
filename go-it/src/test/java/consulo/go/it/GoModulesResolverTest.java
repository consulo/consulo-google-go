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

import consulo.application.Application;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.model.project.ModuleDependencyData;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListenerAdapter;
import consulo.externalSystem.model.task.ExternalSystemTaskType;
import consulo.externalSystem.model.task.TaskData;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.externalSystem.service.project.ProjectData;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.go.modules.GoCommandRunner;
import consulo.go.modules.GoModulesConstants;
import consulo.go.modules.externalSystem.GoDependencyData;
import consulo.go.modules.externalSystem.GoModuleData;
import consulo.go.modules.externalSystem.GoModulesProjectResolver;
import consulo.go.modules.externalSystem.GoModulesTasks;
import consulo.go.modules.externalSystem.GoWorkspaceData;
import consulo.it.HeadlessApplicationExtension;
import consulo.process.ProcessHandlerBuilderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The resolver alone: what it reads from {@code go} and the tree of data it builds, without applying it to a project.
 */
@ExtendWith(HeadlessApplicationExtension.class)
public class GoModulesResolverTest {
    @BeforeEach
    public void requireGo() {
        assumeTrue(GoModulesFixtures.isGoAvailable(), "go is not on PATH");
    }

    @Test
    public void aWorkspaceBecomesOneModulePerUse() throws Exception {
        Path root = GoModulesFixtures.copy("workspace", Files.createTempDirectory("consulo-go-it-resolver"));

        DataNode<ProjectData> projectNode = resolve(root);

        Map<String, DataNode<ModuleData>> modules = new TreeMap<>();
        for (DataNode<ModuleData> moduleNode : ExternalSystemApiUtil.findAll(projectNode, ProjectKeys.MODULE)) {
            modules.put(moduleNode.getData().getExternalName(), moduleNode);
        }
        assertThat(modules).containsOnlyKeys("app", "lib", "tools");

        DataNode<ModuleData> app = modules.get("app");
        assertThat(app.getData().getId()).isEqualTo("example.com/app");
        assertThat(app.getData().getModuleDirPath()).isEqualTo(root.resolve("app").toString());
        assertThat(app.getData().getLinkedExternalProjectPath()).isEqualTo(root.resolve("app/go.mod").toString());

        GoModuleData goModule = ExternalSystemApiUtil.find(app, GoModuleData.KEY).getData();
        assertThat(goModule.getModulePath()).isEqualTo("example.com/app");
        assertThat(goModule.getGoVersion()).isEqualTo("1.22");
        assertThat(goModule.getWorkFile()).isEqualTo(root.resolve("go.work").toString());
        assertThat(goModule.getGoRoot()).as("GOROOT comes from go env").isNotBlank();

        List<String> appModuleDependencies = new ArrayList<>();
        for (DataNode<ModuleDependencyData> dependency : ExternalSystemApiUtil.findAll(app, ProjectKeys.MODULE_DEPENDENCY)) {
            appModuleDependencies.add(dependency.getData().getTarget().getExternalName());
        }
        assertThat(appModuleDependencies).containsExactly("lib");

        assertThat(ExternalSystemApiUtil.findAll(modules.get("lib"), GoDependencyData.KEY))
            .extracting(node -> node.getData().getModulePath())
            .containsExactly("example.org/greeting");
        assertThat(ExternalSystemApiUtil.findAll(modules.get("tools"), GoDependencyData.KEY)).isEmpty();

        List<String> appTasks = new ArrayList<>();
        for (DataNode<TaskData> task : ExternalSystemApiUtil.findAll(app, ProjectKeys.TASK)) {
            String name = task.getData().getName();
            appTasks.add(name);
            // go work sync runs next to go.work, every other task in the module
            Path directory = name.equals(GoModulesTasks.WORK_SYNC) ? root : root.resolve("app");
            assertThat(task.getData().getLinkedExternalProjectPath()).as(name).isEqualTo(directory.toString());
        }
        assertThat(appTasks).containsAll(GoModulesTasks.MODULE_TASKS);

        long workSyncTasks = 0;
        for (DataNode<ModuleData> module : modules.values()) {
            workSyncTasks += ExternalSystemApiUtil.findAll(module, ProjectKeys.TASK).stream()
                .filter(task -> task.getData().getName().equals(GoModulesTasks.WORK_SYNC))
                .count();
        }
        assertThat(workSyncTasks).as("go work sync is offered once per workspace").isEqualTo(1);

        GoWorkspaceData workspace = ExternalSystemApiUtil.find(projectNode, GoWorkspaceData.KEY).getData();
        assertThat(workspace.getModuleNames()).containsExactlyInAnyOrder("app", "lib", "tools");
    }

    @Test
    public void aDirectoryWithoutGoModIsAnError() throws Exception {
        Path empty = Files.createTempDirectory("consulo-go-it-empty");

        StringBuilder output = new StringBuilder();
        assertThatThrownBy(() -> resolve(empty, output)).isInstanceOf(ExternalSystemException.class);
        assertThat(output.toString()).as("go env was asked first").contains("go env -json");
    }

    @Test
    public void aBrokenGoWorkIsReportedWithTheOutputOfGo() throws Exception {
        Path root = GoModulesFixtures.copy("workspace", Files.createTempDirectory("consulo-go-it-broken"));
        Files.writeString(root.resolve("go.work"), "go 1.22\n\nuse (\n\t./missing\n");

        StringBuilder output = new StringBuilder();
        assertThatThrownBy(() -> resolve(root, output)).isInstanceOf(ExternalSystemException.class);
        assertThat(output.toString()).as("the error of go reaches the sync output").contains("go.work");
    }

    private static DataNode<ProjectData> resolve(Path root) {
        return resolve(root, new StringBuilder());
    }

    private static DataNode<ProjectData> resolve(Path root, StringBuilder output) {
        Application application = Application.get();
        GoModulesProjectResolver resolver =
            new GoModulesProjectResolver(new GoCommandRunner(application.getInstance(ProcessHandlerBuilderFactory.class)));
        ExternalSystemTaskId id = ExternalSystemTaskId.create(GoModulesConstants.SYSTEM_ID, ExternalSystemTaskType.RESOLVE_PROJECT, "go-it");
        DataNode<ProjectData> projectNode = resolver.resolveProjectInfo(id,
            ExternalSystemApiUtil.toCanonicalPath(root.toString()),
            false,
            null,
            new ExternalSystemTaskNotificationListenerAdapter() {
                @Override
                public void onTaskOutput(ExternalSystemTaskId taskId, String text, boolean stdOut) {
                    synchronized (output) {
                        output.append(text);
                    }
                }
            });
        assertThat(projectNode).isNotNull();
        return projectNode;
    }
}
