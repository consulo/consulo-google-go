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
import com.goide.runconfig.GoRunningState;
import com.goide.util.GoExecutor;
import com.goide.util.GoHistoryProcessListener;
import consulo.application.util.SystemInfo;
import consulo.container.plugin.PluginManager;
import consulo.execution.debug.DefaultDebugExecutor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.module.Module;
import consulo.process.ExecutionException;
import consulo.process.NopProcessHandler;
import consulo.process.ProcessHandler;
import consulo.process.event.ProcessAdapter;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.File;

public class GoApplicationRunningState extends GoRunningState<GoApplicationConfiguration> {
  private String myOutputFilePath;
  @Nullable
  private GoHistoryProcessListener myHistoryProcessHandler;
  private int myDebugPort = 59090;
  private boolean myCompilationFailed;

  public GoApplicationRunningState(@Nonnull ExecutionEnvironment env, @Nonnull Module module, @Nonnull GoApplicationConfiguration configuration) {
    super(env, module, configuration);
  }

  @Nonnull
  public String getTarget() {
    return myConfiguration.getKind() == GoApplicationConfiguration.Kind.PACKAGE ? myConfiguration.getPackage() : myConfiguration.getFilePath();
  }

  @Nonnull
  public String getGoBuildParams() {
    return myConfiguration.getGoToolParams();
  }

  public boolean isDebug() {
    return DefaultDebugExecutor.EXECUTOR_ID.equals(getEnvironment().getExecutor().getId());
  }

  @Nonnull
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    ProcessHandler processHandler = myCompilationFailed ? new NopProcessHandler() : super.startProcess();
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
          File file = new File(myOutputFilePath);
          if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
          }
        }
      }
    });
    return processHandler;
  }

  @Override
  protected GoExecutor patchExecutor(@Nonnull GoExecutor executor) throws ExecutionException {
    if (isDebug()) {
      File dlv = dlv();
      if (dlv.exists() && !dlv.canExecute()) {
        //noinspection ResultOfMethodCallIgnored
        dlv.setExecutable(true, false);
      }
      String wd = executor.getWorkDirectory();

      return executor.withExePath(dlv.getAbsolutePath())
              .withParameters("--listen=localhost:" + myDebugPort, "--log", wd != null ? "--wd=" + wd : "", "dap",
                              myOutputFilePath, "--");
    }
    return executor.showGoEnvVariables(false).withExePath(myOutputFilePath);
  }

  @Nonnull
  private static File dlv() {
    String dlvPath = System.getProperty("dlv.path");
    if (StringUtil.isNotEmpty(dlvPath)) return new File(dlvPath);

    File pluginPath = PluginManager.getPluginPath(GoApplicationRunningState.class);
    return new File(pluginPath, "dlv/" +
                                (SystemInfo.isMac ? "mac" : SystemInfo.isWindows ? "windows" : "linux") +
                                "/" +
                                GoConstants.DELVE_EXECUTABLE_NAME +
                                (SystemInfo.isWindows ? ".exe" : ""));
  }

  public void setOutputFilePath(@Nonnull String outputFilePath) {
    myOutputFilePath = outputFilePath;
  }

  public void setHistoryProcessHandler(@Nullable GoHistoryProcessListener historyProcessHandler) {
    myHistoryProcessHandler = historyProcessHandler;
  }

  public void setDebugPort(int debugPort) {
    myDebugPort = debugPort;
  }

  public void setCompilationFailed(boolean compilationFailed) {
    myCompilationFailed = compilationFailed;
  }
}
