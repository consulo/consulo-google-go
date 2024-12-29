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

package com.goide.runconfig;

import consulo.annotation.component.ExtensionImpl;
import consulo.execution.configuration.RunProfile;
import consulo.execution.executor.DefaultRunExecutor;
import consulo.execution.runner.DefaultProgramRunner;

import jakarta.annotation.Nonnull;

@ExtensionImpl(id = "goRunner")
public class GoRunner extends DefaultProgramRunner {
  private static final String ID = "GoRunner";

  @Nonnull
  @Override
  public String getRunnerId() {
    return ID;
  }

  @Override
  public boolean canRun(@Nonnull String executorId, @Nonnull RunProfile profile) {
    return DefaultRunExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof GoRunConfigurationBase;
  }
}