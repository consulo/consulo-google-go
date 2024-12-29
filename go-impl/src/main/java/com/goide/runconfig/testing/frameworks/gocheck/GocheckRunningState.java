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

package com.goide.runconfig.testing.frameworks.gocheck;

import com.goide.psi.GoFile;
import com.goide.psi.GoMethodDeclaration;
import com.goide.runconfig.testing.GoTestRunConfiguration;
import com.goide.runconfig.testing.GoTestRunningState;
import com.goide.util.GoExecutor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.module.Module;
import consulo.process.ExecutionException;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedHashSet;

public class GocheckRunningState extends GoTestRunningState {
  public GocheckRunningState(@Nonnull ExecutionEnvironment env,
                             @Nonnull Module module,
                             @Nonnull GoTestRunConfiguration configuration) {
    super(env, module, configuration);
  }

  @Override
  protected GoExecutor patchExecutor(@Nonnull GoExecutor executor) throws ExecutionException {
    return super.patchExecutor(executor).withParameters("-check.vv");
  }

  @Nonnull
  @Override
  protected String buildFilterPatternForFile(GoFile file) {
    Collection<String> testNames = new LinkedHashSet<>();
    for (GoMethodDeclaration method : file.getMethods()) {
      ContainerUtil.addIfNotNull(testNames, GocheckFramework.getGocheckTestName(method));
    }
    return "^" + StringUtil.join(testNames, "|") + "$";
  }

  @Override
  protected void addFilterParameter(@Nonnull GoExecutor executor, String pattern) {
    if (StringUtil.isNotEmpty(pattern)) {
      executor.withParameters("-check.f", pattern);
    }
  }
}
