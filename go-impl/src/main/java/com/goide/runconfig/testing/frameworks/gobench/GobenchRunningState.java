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

package com.goide.runconfig.testing.frameworks.gobench;

import com.goide.psi.GoFile;
import com.goide.psi.GoFunctionDeclaration;
import com.goide.runconfig.testing.GoTestFinder;
import com.goide.runconfig.testing.GoTestRunConfiguration;
import com.goide.runconfig.testing.GoTestRunningState;
import com.goide.util.GoExecutor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.module.Module;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedList;

public class GobenchRunningState extends GoTestRunningState {
  public GobenchRunningState(ExecutionEnvironment env, Module module, GoTestRunConfiguration configuration) {
    super(env, module, configuration);
  }

  @Nonnull
  @Override
  protected String buildFilterPatternForFile(GoFile file) {
    Collection<String> benchmarkNames = new LinkedList<>();
    for (GoFunctionDeclaration function : file.getFunctions()) {
      ContainerUtil.addIfNotNull(benchmarkNames, GoTestFinder.isBenchmarkFunction(function) ? function.getName() : null);
    }
    return "^" + StringUtil.join(benchmarkNames, "|") + "$";
  }

  @Override
  protected void addFilterParameter(@Nonnull GoExecutor executor, String pattern) {
    executor.withParameters("-bench", StringUtil.isEmpty(pattern) ? "." : pattern);
    executor.withParameters("-run", "^$");
  }
}
