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

package com.goide.runconfig;

import com.goide.GoEnvironmentUtil;
import com.goide.runconfig.application.GoApplicationConfiguration;
import com.goide.runconfig.application.GoApplicationRunningState;
import com.goide.util.GoHistoryProcessListener;
import consulo.document.FileDocumentManager;
import consulo.execution.ExecutionManager;
import consulo.execution.ExecutionResult;
import consulo.execution.RunProfileStarter;
import consulo.execution.RunnerAndConfigurationSettings;
import consulo.execution.configuration.RunProfile;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.configuration.RunnerSettings;
import consulo.execution.debug.*;
import consulo.execution.executor.DefaultRunExecutor;
import consulo.execution.runner.BaseProgramRunner;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.runner.RunContentBuilder;
import consulo.execution.ui.RunContentDescriptor;
import consulo.externalService.statistic.UsageTrigger;
import consulo.go.debug.GoDebugProcess;
import consulo.process.ExecutionException;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.collection.ArrayUtil;
import consulo.util.io.FileUtil;
import consulo.util.io.NetUtil;
import consulo.util.lang.StringUtil;
import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class GoBuildingRunner extends BaseProgramRunner<RunnerSettings> {
    private static final String ID = "GoBuildingRunner";

    @Inject
    public GoBuildingRunner() {
    }

    @Override
    public String getRunnerId() {
        return ID;
    }

    @Override
    public boolean canRun(String executorId, RunProfile profile) {
        if (profile instanceof GoApplicationConfiguration) {
            return DefaultRunExecutor.EXECUTOR_ID.equals(executorId) || DefaultDebugExecutor.EXECUTOR_ID.equals(executorId);
        }
        return false;
    }

    @RequiredUIAccess
    @Override
    protected void execute(ExecutionEnvironment environment, RunProfileState state) throws ExecutionException {
        prepare(environment, state).whenComplete((starter, throwable) -> {
            ExecutionManager.getInstance(environment.getProject()).startRunProfile(starter, state, environment);
        });
    }

    private CompletableFuture<RunProfileStarter> prepare(ExecutionEnvironment environment,
                                                         RunProfileState state) throws ExecutionException {
        File outputFile = getOutputFile(environment, (GoApplicationRunningState) state);
        FileDocumentManager.getInstance().saveAllDocuments();

        CompletableFuture<RunProfileStarter> future = new CompletableFuture<>();
        GoHistoryProcessListener historyProcessListener = new GoHistoryProcessListener();
        ((GoApplicationRunningState) state).createCommonExecutor()
            .withParameters("build")
            .withParameterString(((GoApplicationRunningState) state).getGoBuildParams())
            .withParameters("-o", outputFile.getAbsolutePath())
            .withParameters(((GoApplicationRunningState) state).isDebug() ? new String[]{"-gcflags", "-N -l"} : ArrayUtil.EMPTY_STRING_ARRAY)
            .withParameters(((GoApplicationRunningState) state).getTarget())
            .withPresentableName("go build")
            .withProcessListener(historyProcessListener)
            .withProcessListener(new ProcessListener() {

                @Override
                public void processTerminated(ProcessEvent event) {
                    boolean compilationFailed = event.getExitCode() != 0;
                    if (((GoApplicationRunningState) state).isDebug()) {
                        future.complete(new MyDebugStarter(
                            outputFile.getAbsolutePath(),
                            historyProcessListener,
                            compilationFailed));
                    }
                    else {
                        future.complete(new MyRunStarter(outputFile.getAbsolutePath(),
                            historyProcessListener,
                            compilationFailed));
                    }
                }
            })
            .executeWithProgress(false);
        return future;
    }

    private static File getOutputFile(ExecutionEnvironment environment,
                                      GoApplicationRunningState state) throws ExecutionException {
        File outputFile;
        String outputDirectoryPath = state.getConfiguration().getOutputFilePath();
        RunnerAndConfigurationSettings settings = environment.getRunnerAndConfigurationSettings();
        String configurationName = settings != null ? settings.getName() : "application";
        if (StringUtil.isEmpty(outputDirectoryPath)) {
            try {
                outputFile = FileUtil.createTempFile(configurationName, "go", true);
            }
            catch (IOException e) {
                throw new ExecutionException("Cannot create temporary output file", e);
            }
        }
        else {
            File outputDirectory = new File(outputDirectoryPath);
            if (outputDirectory.isDirectory() || !outputDirectory.exists() && outputDirectory.mkdirs()) {
                outputFile = new File(outputDirectoryPath, GoEnvironmentUtil.getBinaryFileNameForPath(configurationName));
                try {
                    if (!outputFile.exists() && !outputFile.createNewFile()) {
                        throw new ExecutionException("Cannot create output file " + outputFile.getAbsolutePath());
                    }
                }
                catch (IOException e) {
                    throw new ExecutionException("Cannot create output file " + outputFile.getAbsolutePath());
                }
            }
            else {
                throw new ExecutionException("Cannot create output file in " + outputDirectory.getAbsolutePath());
            }
        }
        if (!prepareFile(outputFile)) {
            throw new ExecutionException("Cannot make temporary file executable " + outputFile.getAbsolutePath());
        }
        return outputFile;
    }

    private static boolean prepareFile(File file) {
        try {
            FileUtil.writeToFile(file, new byte[]{0x7F, 'E', 'L', 'F'});
        }
        catch (IOException e) {
            return false;
        }
        return file.setExecutable(true);
    }

    private class MyDebugStarter implements RunProfileStarter {
        private final String myOutputFilePath;
        private final GoHistoryProcessListener myHistoryProcessListener;
        private final boolean myCompilationFailed;


        private MyDebugStarter(String outputFilePath,
                               GoHistoryProcessListener historyProcessListener,
                               boolean compilationFailed) {
            myOutputFilePath = outputFilePath;
            myHistoryProcessListener = historyProcessListener;
            myCompilationFailed = compilationFailed;
        }

        @Override
        public CompletableFuture<RunContentDescriptor> executeAsync(RunProfileState state, ExecutionEnvironment env) throws ExecutionException {
            if (state instanceof GoApplicationRunningState) {
                final int port;
                try {
                    port = NetUtil.findAvailableSocketPort();
                }
                catch (IOException e) {
                    throw new ExecutionException(e);
                }

                FileDocumentManager.getInstance().saveAllDocuments();
                ((GoApplicationRunningState) state).setHistoryProcessHandler(myHistoryProcessListener);
                ((GoApplicationRunningState) state).setDebugPort(port);

                // start debugger
                ExecutionResult executionResult = state.execute(env.getExecutor(), GoBuildingRunner.this);

                UsageTrigger.trigger("go.dlv.debugger");

                XDebugSession session = XDebuggerManager.getInstance(env.getProject()).startSession(env, new XDebugProcessStarter() {
                    @Override
                    public XDebugProcess start(XDebugSession session) throws ExecutionException {
                        GoDebugProcess process = new GoDebugProcess(session, port, myOutputFilePath);
                        process.start();
                        return process;
                    }
                });
                return CompletableFuture.completedFuture(session.getRunContentDescriptor());
            }
            return CompletableFuture.completedFuture(null);
        }
    }

    private class MyRunStarter implements RunProfileStarter {
        private final String myOutputFilePath;
        private final GoHistoryProcessListener myHistoryProcessListener;


        private MyRunStarter(String outputFilePath,
                             GoHistoryProcessListener historyProcessListener,
                             boolean compilationFailed) {
            myOutputFilePath = outputFilePath;
            myHistoryProcessListener = historyProcessListener;
        }

        @Override
        public CompletableFuture<RunContentDescriptor> executeAsync(RunProfileState state, ExecutionEnvironment env) throws ExecutionException {
            if (state instanceof GoApplicationRunningState) {
                FileDocumentManager.getInstance().saveAllDocuments();
                ((GoApplicationRunningState) state).setHistoryProcessHandler(myHistoryProcessListener);
                ExecutionResult executionResult = state.execute(env.getExecutor(), GoBuildingRunner.this);
                return CompletableFuture.completedFuture(new RunContentBuilder(executionResult, env).showRunContent(env.getContentToReuse()));
            }
            return CompletableFuture.completedFuture(null);
        }
    }
}
