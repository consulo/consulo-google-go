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

import com.goide.compiler.GoCompilerRunner;
import com.goide.runconfig.GoRunningState;
import com.goide.util.GoExecutor;
import com.goide.util.GoHistoryProcessListener;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.module.Module;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.Objects;

public class GoApplicationRunningState extends GoRunningState<GoApplicationConfiguration> {
    @Nullable
    private GoHistoryProcessListener myHistoryProcessHandler;

    public GoApplicationRunningState(ExecutionEnvironment env, Module module, GoApplicationConfiguration configuration) {
        super(env, module, configuration);
    }

    public String getTarget() {
        return myConfiguration.getKind() == GoApplicationConfiguration.Kind.PACKAGE ? myConfiguration.getPackage() : myConfiguration.getFilePath();
    }

    public String getGoBuildParams() {
        return myConfiguration.getGoToolParams();
    }

    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        ProcessHandler processHandler = super.startProcess();
        processHandler.addProcessListener(new ProcessListener() {
            @Override
            public void startNotified(ProcessEvent event) {
                if (myHistoryProcessHandler != null) {
                    myHistoryProcessHandler.apply(processHandler);
                }
            }
        });
        deleteOutputFileOnExit(processHandler);
        return processHandler;
    }

    /**
     * The built program with its parameters, working directory and environment - as it is run, and as a debugger
     * launches it.
     */
    public GeneralCommandLine createProgramCommandLine() throws ExecutionException {
        return patchExecutor(createCommonExecutor()).withParameterString(myConfiguration.getParams()).createCommandLine();
    }

    /**
     * A binary built to a temporary place is gone once the process which runs it - the program or its debugger - ends.
     */
    public void deleteOutputFileOnExit(ProcessHandler processHandler) {
        File outputFile = getOutputFile();
        processHandler.addProcessListener(new ProcessListener() {
            @Override
            public void processTerminated(ProcessEvent event) {
                if (StringUtil.isEmpty(myConfiguration.getOutputFilePath()) && outputFile.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    outputFile.delete();
                }
            }
        });
    }

    @Override
    protected GoExecutor patchExecutor(GoExecutor executor) throws ExecutionException {
        return executor.showGoEnvVariables(false).withExePath(getOutputFile().getAbsolutePath());
    }

    private File getOutputFile() {
        return Objects.requireNonNull(getEnvironment().getUserData(GoCompilerRunner.OUTPUT_FILE), "Output File must be set");
    }

    public void setHistoryProcessHandler(@Nullable GoHistoryProcessListener historyProcessHandler) {
        myHistoryProcessHandler = historyProcessHandler;
    }
}
