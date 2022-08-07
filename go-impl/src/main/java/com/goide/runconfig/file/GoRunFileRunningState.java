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

package com.goide.runconfig.file;

import com.goide.runconfig.GoRunningState;
import com.goide.util.GoExecutor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.module.Module;
import consulo.process.ExecutionException;

import javax.annotation.Nonnull;

public class GoRunFileRunningState extends GoRunningState<GoRunFileConfiguration> {
  public GoRunFileRunningState(@Nonnull ExecutionEnvironment env, @Nonnull Module module, GoRunFileConfiguration configuration) {
    super(env, module, configuration);
  }

  @Override
  protected GoExecutor patchExecutor(@Nonnull GoExecutor executor) throws ExecutionException {
    return executor
        .withParameters("run")
        .withParameterString(myConfiguration.getGoToolParams())
        .withParameters(myConfiguration.getFilePath());
  }
}
