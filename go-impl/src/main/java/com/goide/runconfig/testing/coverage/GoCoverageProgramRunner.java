/*
 * Copyright 2013-2015 Sergey Ignatov, Alexander Zolotov, Florin Patan
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

package com.goide.runconfig.testing.coverage;

import com.goide.runconfig.testing.GoTestRunConfiguration;
import com.goide.runconfig.testing.GoTestRunningState;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.FileDocumentManager;
import consulo.execution.ExecutionResult;
import consulo.execution.configuration.ConfigurationInfoProvider;
import consulo.execution.configuration.RunProfile;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.configuration.RunnerSettings;
import consulo.execution.coverage.CoverageEnabledConfiguration;
import consulo.execution.coverage.CoverageExecutor;
import consulo.execution.coverage.CoverageHelper;
import consulo.execution.coverage.CoverageRunnerData;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.runner.GenericProgramRunner;
import consulo.execution.runner.RunContentBuilder;
import consulo.execution.ui.RunContentDescriptor;
import consulo.process.ExecutionException;
import consulo.util.lang.ObjectUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class GoCoverageProgramRunner extends GenericProgramRunner {
  private static final String ID = "GoCoverageProgramRunner";

  @Nonnull
  @Override
  public String getRunnerId() {
    return ID;
  }

  @Override
  public boolean canRun(@Nonnull String executorId, @Nonnull RunProfile profile) {
    return executorId.equals(CoverageExecutor.EXECUTOR_ID) && profile instanceof GoTestRunConfiguration;
  }

  @Override
  public RunnerSettings createConfigurationData(ConfigurationInfoProvider settingsProvider) {
    return new CoverageRunnerData();
  }

  @Nullable
  @Override
  protected RunContentDescriptor doExecute(@Nonnull RunProfileState state, @Nonnull ExecutionEnvironment environment)
    throws ExecutionException {
    assert state instanceof GoTestRunningState;
    GoTestRunningState runningState = (GoTestRunningState)state;
    GoTestRunConfiguration runConfiguration = ObjectUtil.tryCast(environment.getRunProfile(), GoTestRunConfiguration.class);
    if (runConfiguration == null) {
      return null;
    }
    FileDocumentManager.getInstance().saveAllDocuments();
    CoverageEnabledConfiguration coverageEnabledConfiguration = CoverageEnabledConfiguration.getOrCreate(runConfiguration);
    runningState.setCoverageFilePath(coverageEnabledConfiguration.getCoverageFilePath());

    ExecutionResult executionResult = state.execute(environment.getExecutor(), this);
    if (executionResult == null) {
      return null;
    }
    CoverageHelper.attachToProcess(runConfiguration, executionResult.getProcessHandler(), environment.getRunnerSettings());
    return new RunContentBuilder(executionResult, environment).showRunContent(environment.getContentToReuse());
  }
}
