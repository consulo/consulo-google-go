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

import com.goide.util.GoExecutor;
import consulo.execution.configuration.CommandLineState;
import consulo.execution.process.ProcessTerminatedListener;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.module.Module;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.local.ProcessHandlerFactory;

import javax.annotation.Nonnull;

public abstract class GoRunningState<T extends GoRunConfigurationBase<?>> extends CommandLineState {
  @Nonnull
  protected final Module myModule;

  @Nonnull
  public T getConfiguration() {
    return myConfiguration;
  }

  @Nonnull
  protected final T myConfiguration;

  public GoRunningState(@Nonnull ExecutionEnvironment env, @Nonnull Module module, @Nonnull T configuration) {
    super(env);
    myModule = module;
    myConfiguration = configuration;
    addConsoleFilters(new GoConsoleFilter(myConfiguration.getProject(), myModule, myConfiguration.getWorkingDirectoryUrl()));
  }

  @Nonnull
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    GoExecutor executor = patchExecutor(createCommonExecutor());
    GeneralCommandLine commandLine = executor.withParameterString(myConfiguration.getParams()).createCommandLine();
    ProcessHandler handler = ProcessHandlerFactory.getInstance().createKillableProcessHandler(commandLine);
    ProcessTerminatedListener.attach(handler);
    return handler;
  }

  @Nonnull
  public GoExecutor createCommonExecutor() {
    return GoExecutor.in(myModule).withWorkDirectory(myConfiguration.getWorkingDirectory())
      .withExtraEnvironment(myConfiguration.getCustomEnvironment())
      .withPassParentEnvironment(myConfiguration.isPassParentEnvironment());
  }

  protected GoExecutor patchExecutor(@Nonnull GoExecutor executor) throws ExecutionException {
    return executor;
  }
}
