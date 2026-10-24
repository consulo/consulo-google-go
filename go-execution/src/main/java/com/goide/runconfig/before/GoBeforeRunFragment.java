/*
 * Copyright 2013-2016 Sergey Ignatov, Alexander Zolotov, Florin Patan
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

package com.goide.runconfig.before;

import com.goide.runconfig.GoRunConfigurationBase;
import com.goide.sdk.GoSdkService;
import com.goide.util.GoExecutor;
import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.ConfigurationException;
import consulo.dataContext.DataContext;
import consulo.disposer.Disposable;
import consulo.execution.BeforeRunFragment;
import consulo.execution.configuration.RunConfiguration;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.go.icon.GoIconGroup;
import consulo.go.localize.GoLocalize;
import consulo.language.editor.ui.EditorBox;
import consulo.language.editor.ui.EditorBoxBuilderFactory;
import consulo.language.editor.ui.textCompletion.DefaultTextCompletionValueDescriptor;
import consulo.language.editor.ui.textCompletion.ValuesCompletionProvider;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.project.Project;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.image.Image;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFileManager;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

@ExtensionImpl
public class GoBeforeRunFragment implements BeforeRunFragment<GoCommandBeforeRunTask> {
    public static final String ID = "GoBeforeRunTask";

    private static final List<String> PREDEFINED_COMMANDS = List.of("vet", "test -i");

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public LocalizeValue getName() {
        return GoLocalize.beforeRunGoCommandName();
    }

    @Override
    public LocalizeValue getDescription(GoCommandBeforeRunTask task) {
        return GoLocalize.beforeRunGoCommandDescription(StringUtil.notNullize(task.getCommand()));
    }

    @Nullable
    @Override
    public Image getIcon(RunConfiguration configuration) {
        return GoIconGroup.goapp();
    }

    @Override
    public Class<GoCommandBeforeRunTask> getStateClass() {
        return GoCommandBeforeRunTask.class;
    }

    @Nullable
    @Override
    public GoCommandBeforeRunTask createTask(RunConfiguration configuration) {
        return configuration instanceof GoRunConfigurationBase ? new GoCommandBeforeRunTask() : null;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<GoCommandBeforeRunTask> createEditor(RunConfiguration configuration, Disposable parent) {
        Project project = configuration.getProject();
        EditorBox command = project.getApplication()
            .getInstance(EditorBoxBuilderFactory.class)
            .create(project)
            .completion(new ValuesCompletionProvider<>(new DefaultTextCompletionValueDescriptor.StringValueDescriptor(), PREDEFINED_COMMANDS))
            .build();

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(GoLocalize.beforeRunGoCommandLabel(), command);
            }

            @RequiredUIAccess
            @Override
            public void reset(GoCommandBeforeRunTask task) {
                command.setValue(StringUtil.notNullize(task.getCommand()));
            }

            @RequiredUIAccess
            @Override
            public void apply(GoCommandBeforeRunTask task) throws ConfigurationException {
                String value = StringUtil.notNullize(command.getValue()).trim();
                if (value.isEmpty()) {
                    throw new ConfigurationException(GoLocalize.beforeRunGoCommandErrorEmpty());
                }
                if (value.startsWith("go ")) {
                    throw new ConfigurationException(GoLocalize.beforeRunGoCommandErrorGoPrefix());
                }
                task.setCommand(value);
            }
        };
    }

    @Override
    public boolean canExecuteTask(RunConfiguration configuration, GoCommandBeforeRunTask task) {
        if (configuration instanceof GoRunConfigurationBase<?> goRunConfiguration) {
            Module module = goRunConfiguration.getConfigurationModule().getModule();
            GoSdkService sdkService = GoSdkService.getInstance(configuration.getProject());
            if (sdkService.isGoModule(module)) {
                return StringUtil.isNotEmpty(sdkService.getSdkHomePath(module)) && StringUtil.isNotEmpty(task.getCommand());
            }
        }
        return false;
    }

    @Override
    public CompletableFuture<?> executeAsync(UIAccess uiAccess,
                                             DataContext context,
                                             RunConfiguration configuration,
                                             ExecutionEnvironment env,
                                             GoCommandBeforeRunTask task) {
        if (!(configuration instanceof GoRunConfigurationBase<?> goRunConfiguration)) {
            return CompletableFuture.failedFuture(new CancellationException());
        }

        String command = task.getCommand();
        Module module = goRunConfiguration.getConfigurationModule().getModule();
        Project project = configuration.getProject();
        String workingDirectory = goRunConfiguration.getWorkingDirectory();
        if (StringUtil.isEmpty(command) || project.isDisposed() || !GoSdkService.getInstance(project).isGoModule(module)) {
            return CompletableFuture.failedFuture(new CancellationException());
        }

        CompletableFuture<Void> result = new CompletableFuture<>();
        uiAccess.give(() -> GoExecutor.in(module).withParameterString(command)
            .withWorkDirectory(workingDirectory)
            .showOutputOnError()
            .showNotifications(false, true)
            .withPresentableName("Executing `" + task + "`")
            .withProcessListener(new ProcessListener() {
                @Override
                public void processTerminated(ProcessEvent event) {
                    if (event.getExitCode() == 0) {
                        result.complete(null);
                    }
                    else {
                        result.completeExceptionally(new CancellationException());
                    }
                }
            })
            .executeWithProgress(false, success -> {
                VirtualFileManager.getInstance().asyncRefresh(null);
                if (Boolean.TRUE.equals(success)) {
                    result.complete(null);
                }
                else {
                    result.completeExceptionally(new CancellationException());
                }
            }));
        return result;
    }
}
