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
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListenerAdapter;
import consulo.externalSystem.model.task.ExternalSystemTaskType;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.go.modules.GoCommandRunner;
import consulo.go.modules.GoModulesConstants;
import consulo.go.modules.GoToolchain;
import consulo.go.modules.externalSystem.GoModulesTaskManager;
import consulo.go.modules.externalSystem.GoModulesTasks;
import consulo.go.modules.setting.GoModulesExecutionSettings;
import consulo.it.HeadlessApplicationExtension;
import consulo.process.ProcessHandlerBuilderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tasks are {@code go} sub-commands run in the directory of a module of the workspace.
 */
@ExtendWith(HeadlessApplicationExtension.class)
public class GoModulesTaskManagerTest {
    private Path myRoot;

    @BeforeEach
    public void prepare() throws Exception {
        assumeTrue(GoModulesFixtures.isGoAvailable(), "go is not on PATH");
        myRoot = GoModulesFixtures.copy("workspace", Files.createTempDirectory("consulo-go-it-tasks"));
    }

    @Test
    public void buildTestAndVetRunInTheModule() {
        StringBuilder output = new StringBuilder();
        run(myRoot.resolve("app"), GoModulesTasks.BUILD, List.of(), output);
        run(myRoot.resolve("app"), GoModulesTasks.VET, List.of(), output);
        run(myRoot.resolve("lib"), GoModulesTasks.TEST, List.of("-run", "TestShout", "./..."), output);

        assertThat(output.toString())
            .contains("go build ./...")
            .contains("go test -run TestShout ./...")
            .contains("ok")
            .contains("example.com/lib/util");
    }

    @Test
    public void aFailingCommandFailsTheTaskWithTheCompilerOutput() throws Exception {
        Files.writeString(myRoot.resolve("tools/cmd/lint/broken.go"), "package main\n\nvar broken = undefinedName\n");

        StringBuilder output = new StringBuilder();
        assertThatThrownBy(() -> run(myRoot.resolve("tools"), GoModulesTasks.BUILD, List.of(), output))
            .isInstanceOf(ExternalSystemException.class);
        assertThat(output.toString()).as("the compiler error reaches the task output").contains("broken.go", "undefinedName");
    }

    private static void run(Path directory, String taskName, List<String> parameters, StringBuilder output) {
        GoModulesTaskManager taskManager =
            new GoModulesTaskManager(new GoCommandRunner(Application.get().getInstance(ProcessHandlerBuilderFactory.class)));
        GoModulesExecutionSettings settings = new GoModulesExecutionSettings(GoToolchain.fromPath(), null, false, Map.of());
        ExternalSystemTaskId id = ExternalSystemTaskId.create(GoModulesConstants.SYSTEM_ID, ExternalSystemTaskType.EXECUTE_TASK, "go-it");
        taskManager.executeTasks(id, List.of(taskName), directory.toString(), settings, List.of(), parameters, null,
            new ExternalSystemTaskNotificationListenerAdapter() {
                @Override
                public void onTaskOutput(ExternalSystemTaskId taskId, String text, boolean stdOut) {
                    synchronized (output) {
                        output.append(text);
                    }
                }
            });
    }
}
