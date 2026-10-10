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

import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.externalSystem.task.ExternalSystemTaskManager;
import consulo.go.localize.GoLocalize;
import consulo.go.modules.GoCommandRunner;
import consulo.go.modules.GoModulesProjects;
import consulo.go.modules.GoToolchain;
import consulo.go.modules.setting.GoModulesExecutionSettings;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

/**
 * Runs a task as {@code go <task name> [parameters]} in the directory of the module it belongs to.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public class GoModulesTaskManager implements ExternalSystemTaskManager<GoModulesExecutionSettings> {
    private final GoCommandRunner myRunner;

    public GoModulesTaskManager(GoCommandRunner runner) {
        myRunner = runner;
    }

    @Override
    public void executeTasks(ExternalSystemTaskId id,
                             List<String> taskNames,
                             String projectPath,
                             @Nullable GoModulesExecutionSettings settings,
                             List<String> vmOptions,
                             List<String> scriptParameters,
                             @Nullable String debuggerSetup,
                             ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        GoToolchain toolchain = settings != null ? settings.getToolchain() : GoToolchain.fromPath();
        if (toolchain == null) {
            throw new ExternalSystemException(GoLocalize.goModulesGoNotFound().get());
        }
        Path directory = GoModulesProjects.toDirectory(Path.of(projectPath));
        for (String taskName : taskNames) {
            GeneralCommandLine commandLine = toolchain.createCommandLine(directory, GoModulesTasks.arguments(taskName, scriptParameters));
            listener.onTaskOutput(id, commandLine.getCommandLineString() + "\n", true);

            GoCommandRunner.Result result;
            try {
                result = myRunner.run(id, commandLine, (text, stdout) -> listener.onTaskOutput(id, text, stdout), true);
            }
            catch (ExecutionException e) {
                throw new ExternalSystemException(e);
            }
            if (!result.isSuccess()) {
                throw new ExternalSystemException(GoLocalize.goModulesTaskFailed("go " + taskName, result.exitCode()).get());
            }
        }
    }

    @Override
    public boolean cancelTask(ExternalSystemTaskId id, ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        return myRunner.cancel(id);
    }
}
