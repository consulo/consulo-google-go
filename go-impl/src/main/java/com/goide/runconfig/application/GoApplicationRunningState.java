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

package com.goide.runconfig.application;

import com.goide.GoConstants;
import com.goide.compiler.GoCompilerRunner;
import com.goide.runconfig.GoRunningState;
import com.goide.util.GoExecutor;
import com.goide.util.GoHistoryProcessListener;
import consulo.container.plugin.PluginManager;
import consulo.execution.debug.DefaultDebugExecutor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.module.Module;
import consulo.platform.Platform;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.Objects;

public class GoApplicationRunningState extends GoRunningState<GoApplicationConfiguration> {
    @Nullable
    private GoHistoryProcessListener myHistoryProcessHandler;
    private int myDebugPort = 59090;

    public GoApplicationRunningState(ExecutionEnvironment env, Module module, GoApplicationConfiguration configuration) {
        super(env, module, configuration);
    }

    public String getTarget() {
        return myConfiguration.getKind() == GoApplicationConfiguration.Kind.PACKAGE ? myConfiguration.getPackage() : myConfiguration.getFilePath();
    }

    public String getGoBuildParams() {
        return myConfiguration.getGoToolParams();
    }

    public boolean isDebug() {
        return DefaultDebugExecutor.EXECUTOR_ID.equals(getEnvironment().getExecutor().getId());
    }

    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        File outputFile = Objects.requireNonNull(getEnvironment().getUserData(GoCompilerRunner.OUTPUT_FILE), "Output File must be set");

        ProcessHandler processHandler = super.startProcess();
        processHandler.addProcessListener(new ProcessListener() {
            @Override
            public void startNotified(ProcessEvent event) {
                if (myHistoryProcessHandler != null) {
                    myHistoryProcessHandler.apply(processHandler);
                }
            }

            @Override
            public void processTerminated(ProcessEvent event) {
                if (StringUtil.isEmpty(myConfiguration.getOutputFilePath())) {
                    if (outputFile.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        outputFile.delete();
                    }
                }
            }
        });
        return processHandler;
    }

    @Override
    protected GoExecutor patchExecutor(GoExecutor executor) throws ExecutionException {
        File outputFile = Objects.requireNonNull(getEnvironment().getUserData(GoCompilerRunner.OUTPUT_FILE), "Output File must be set");

        if (isDebug()) {
            File dlv = dlv();
            if (dlv.exists() && !dlv.canExecute()) {
                //noinspection ResultOfMethodCallIgnored
                dlv.setExecutable(true, false);
            }
            String wd = executor.getWorkDirectory();

            return executor.withExePath(dlv.getAbsolutePath())
                .withParameters("--listen=localhost:" + myDebugPort, "--log", wd != null ? "--wd=" + wd : "", "dap",
                    outputFile.getAbsolutePath(), "--");
        }
        return executor.showGoEnvVariables(false).withExePath(outputFile.getAbsolutePath());
    }

    private static File dlv() {
        Platform platform = Platform.current();

        String dlvPath = platform.jvm().getRuntimeProperty("dlv.path");
        if (StringUtil.isNotEmpty(dlvPath)) {
            return new File(dlvPath);
        }

        String prefix = platform.os().fileNamePrefix();
        String suffix = platform.jvm().arch().fileNameSuffix();

        String dirName = prefix + "-delve" + suffix;
        File pluginPath = PluginManager.getPluginPath(GoApplicationRunningState.class);
        return new File(new File(pluginPath, dirName),
            platform.os().isWindows() ? GoConstants.DELVE_EXECUTABLE_NAME + ".exe" : GoConstants.DELVE_EXECUTABLE_NAME);
    }

    public void setHistoryProcessHandler(@Nullable GoHistoryProcessListener historyProcessHandler) {
        myHistoryProcessHandler = historyProcessHandler;
    }

    public void setDebugPort(int debugPort) {
        myDebugPort = debugPort;
    }
}
