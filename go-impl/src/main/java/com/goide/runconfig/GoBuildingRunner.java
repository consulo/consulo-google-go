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
import consulo.annotation.component.ExtensionImpl;
import consulo.application.concurrent.ApplicationConcurrency;
import consulo.document.FileDocumentManager;
import consulo.execution.ExecutionResult;
import consulo.execution.RunProfileStarter;
import consulo.execution.RunnerAndConfigurationSettings;
import consulo.execution.configuration.RunProfile;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.debug.*;
import consulo.execution.executor.DefaultRunExecutor;
import consulo.execution.runner.AsyncGenericProgramRunner;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.runner.RunContentBuilder;
import consulo.execution.ui.RunContentDescriptor;
import consulo.externalService.statistic.UsageTrigger;
import consulo.go.debug.GoDebugProcess;
import consulo.process.ExecutionException;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.util.collection.ArrayUtil;
import consulo.util.concurrent.AsyncResult;
import consulo.util.io.FileUtil;
import consulo.util.io.NetUtil;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;

@ExtensionImpl(order = "before goRunner")
public class GoBuildingRunner extends AsyncGenericProgramRunner {
  private static final String ID = "GoBuildingRunner";

  private final ApplicationConcurrency myApplicationConcurrency;

  @Inject
  public GoBuildingRunner(ApplicationConcurrency applicationConcurrency) {
    myApplicationConcurrency = applicationConcurrency;
  }

  @Nonnull
  @Override
  public String getRunnerId() {
    return ID;
  }

  @Override
  public boolean canRun(@Nonnull String executorId, @Nonnull RunProfile profile) {
    if (profile instanceof GoApplicationConfiguration) {
      return DefaultRunExecutor.EXECUTOR_ID.equals(executorId) || DefaultDebugExecutor.EXECUTOR_ID.equals(executorId);
    }
    return false;
  }

  @Nonnull
  @Override
  protected AsyncResult<RunProfileStarter> prepare(@Nonnull ExecutionEnvironment environment,
                                                   @Nonnull RunProfileState state) throws ExecutionException {
    File outputFile = getOutputFile(environment, (GoApplicationRunningState)state);
    FileDocumentManager.getInstance().saveAllDocuments();

    AsyncResult<RunProfileStarter> buildingPromise = new AsyncResult<>();
    GoHistoryProcessListener historyProcessListener = new GoHistoryProcessListener();
    ((GoApplicationRunningState)state).createCommonExecutor()
                                      .withParameters("build")
                                      .withParameterString(((GoApplicationRunningState)state).getGoBuildParams())
                                      .withParameters("-o", outputFile.getAbsolutePath())
                                      .withParameters(((GoApplicationRunningState)state).isDebug() ? new String[]{"-gcflags", "-N -l"} : ArrayUtil.EMPTY_STRING_ARRAY)
                                      .withParameters(((GoApplicationRunningState)state).getTarget())
                                      .withPresentableName("go build")
                                      .withProcessListener(historyProcessListener)
                                      .withProcessListener(new ProcessListener() {

                                        @Override
                                        public void processTerminated(ProcessEvent event) {
                                          boolean compilationFailed = event.getExitCode() != 0;
                                          if (((GoApplicationRunningState)state).isDebug()) {
                                            buildingPromise.setDone(new MyDebugStarter(myApplicationConcurrency,
                                                                                       outputFile.getAbsolutePath(),
                                                                                       historyProcessListener,
                                                                                       compilationFailed));
                                          }
                                          else {
                                            buildingPromise.setDone(new MyRunStarter(outputFile.getAbsolutePath(),
                                                                                     historyProcessListener,
                                                                                     compilationFailed));
                                          }
                                        }
                                      })
                                      .executeWithProgress(false);
    return buildingPromise;
  }

  @Nonnull
  private static File getOutputFile(@Nonnull ExecutionEnvironment environment,
                                    @Nonnull GoApplicationRunningState state) throws ExecutionException {
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

  private static boolean prepareFile(@Nonnull File file) {
    try {
      FileUtil.writeToFile(file, new byte[]{0x7F, 'E', 'L', 'F'});
    }
    catch (IOException e) {
      return false;
    }
    return file.setExecutable(true);
  }

  private class MyDebugStarter extends RunProfileStarter {
    private final ApplicationConcurrency myApplicationConcurrency;
    private final String myOutputFilePath;
    private final GoHistoryProcessListener myHistoryProcessListener;
    private final boolean myCompilationFailed;


    private MyDebugStarter(ApplicationConcurrency applicationConcurrency,
                           @Nonnull String outputFilePath,
                           @Nonnull GoHistoryProcessListener historyProcessListener,
                           boolean compilationFailed) {
      myApplicationConcurrency = applicationConcurrency;
      myOutputFilePath = outputFilePath;
      myHistoryProcessListener = historyProcessListener;
      myCompilationFailed = compilationFailed;
    }

    @Nullable
    @Override
    public RunContentDescriptor execute(@Nonnull RunProfileState state, @Nonnull ExecutionEnvironment env) throws ExecutionException {
      if (state instanceof GoApplicationRunningState) {
        final int port;
        try {
          port = NetUtil.findAvailableSocketPort();
        }
        catch (IOException e) {
          throw new ExecutionException(e);
        }

        FileDocumentManager.getInstance().saveAllDocuments();
        ((GoApplicationRunningState)state).setHistoryProcessHandler(myHistoryProcessListener);
        ((GoApplicationRunningState)state).setOutputFilePath(myOutputFilePath);
        ((GoApplicationRunningState)state).setDebugPort(port);
        ((GoApplicationRunningState)state).setCompilationFailed(myCompilationFailed);

        // start debugger
        ExecutionResult executionResult = state.execute(env.getExecutor(), GoBuildingRunner.this);
        if (executionResult == null) {
          throw new ExecutionException("Cannot run debugger");
        }

        UsageTrigger.trigger("go.dlv.debugger");

        return XDebuggerManager.getInstance(env.getProject()).startSession(env, new XDebugProcessStarter() {
          @Nonnull
          @Override
          public XDebugProcess start(@Nonnull XDebugSession session) throws ExecutionException {
            GoDebugProcess process = new GoDebugProcess(session, port, myOutputFilePath);
            process.start();
            return process;
          }
        }).getRunContentDescriptor();
      }
      return null;
    }
  }

  private class MyRunStarter extends RunProfileStarter {
    private final String myOutputFilePath;
    private final GoHistoryProcessListener myHistoryProcessListener;
    private final boolean myCompilationFailed;


    private MyRunStarter(@Nonnull String outputFilePath,
                         @Nonnull GoHistoryProcessListener historyProcessListener,
                         boolean compilationFailed) {
      myOutputFilePath = outputFilePath;
      myHistoryProcessListener = historyProcessListener;
      myCompilationFailed = compilationFailed;
    }

    @Nullable
    @Override
    public RunContentDescriptor execute(@Nonnull RunProfileState state, @Nonnull ExecutionEnvironment env) throws ExecutionException {
      if (state instanceof GoApplicationRunningState) {
        FileDocumentManager.getInstance().saveAllDocuments();
        ((GoApplicationRunningState)state).setHistoryProcessHandler(myHistoryProcessListener);
        ((GoApplicationRunningState)state).setOutputFilePath(myOutputFilePath);
        ((GoApplicationRunningState)state).setCompilationFailed(myCompilationFailed);
        ExecutionResult executionResult = state.execute(env.getExecutor(), GoBuildingRunner.this);
        return executionResult != null ? new RunContentBuilder(executionResult, env).showRunContent(env.getContentToReuse()) : null;
      }
      return null;
    }
  }
}
