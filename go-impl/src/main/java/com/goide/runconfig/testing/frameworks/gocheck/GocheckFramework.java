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

package com.goide.runconfig.testing.frameworks.gocheck;

import com.goide.psi.GoFile;
import com.goide.psi.GoFunctionOrMethodDeclaration;
import com.goide.psi.GoMethodDeclaration;
import com.goide.psi.impl.GoPsiImplUtil;
import com.goide.runconfig.testing.*;
import com.goide.stubs.index.GoPackagesIndex;
import com.goide.util.GoUtil;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.test.TestConsoleProperties;
import consulo.execution.test.sm.runner.OutputToGeneralTestEventsConverter;
import consulo.language.psi.PsiFile;
import consulo.language.psi.stub.StubIndex;
import consulo.module.Module;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.regex.Pattern;

public class GocheckFramework extends GoTestFramework {
  public static final String NAME = "gocheck";
  public static final GocheckFramework INSTANCE = new GocheckFramework();
  private static final Pattern GO_CHECK_IMPORT_PATH = Pattern.compile("gopkg\\.in/check\\.v\\d+");
  private static final Pattern GO_CHECK_GITHUB_IMPORT_PATH = Pattern.compile("github\\.com/go-check/check\\.v\\d+");

  private GocheckFramework() {
  }

  @Nullable
  public static String getGocheckTestName(@Nonnull GoMethodDeclaration method) {
    String methodName = GoTestFunctionType.fromName(method.getName()) == GoTestFunctionType.TEST ? method.getName() : null;
    if (methodName != null) {
      String suiteName = GoPsiImplUtil.getText(method.getReceiverType());
      if (!suiteName.isEmpty()) {
        return suiteName + "." + methodName;
      }
    }
    return null;
  }

  @Nonnull
  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean isAvailable(@Nullable Module module) {
    if (module == null) return false;
    return !StubIndex.getInstance().processElements(GoPackagesIndex.KEY, "check", module.getProject(),
                                                    GoUtil.goPathResolveScope(module, null), GoFile.class,
                                                    file -> !isGoCheckImportPath(file.getImportPath(true)));
  }

  private static boolean isGoCheckImportPath(String importPath) {
    if (importPath == null) return false;
    return GO_CHECK_IMPORT_PATH.matcher(importPath).matches() || GO_CHECK_GITHUB_IMPORT_PATH.matcher(importPath).matches();
  }

  @Override
  public boolean isAvailableOnFile(@Nullable PsiFile file) {
    if (!GoTestFinder.isTestFile(file)) {
      return false;
    }
    for (String importPath : ((GoFile)file).getImportedPackagesMap().keySet()) {
      if (isGoCheckImportPath(importPath)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isAvailableOnFunction(@Nullable GoFunctionOrMethodDeclaration functionOrMethodDeclaration) {
    return functionOrMethodDeclaration instanceof GoMethodDeclaration && GoTestFinder.isTestOrExampleFunction(functionOrMethodDeclaration);
  }

  @Nonnull
  @Override
  protected GoTestRunningState newRunningState(@Nonnull ExecutionEnvironment env,
                                               @Nonnull Module module,
                                               @Nonnull GoTestRunConfiguration runConfiguration) {
    return new GocheckRunningState(env, module, runConfiguration);
  }

  @Nonnull
  @Override
  public OutputToGeneralTestEventsConverter createTestEventsConverter(@Nonnull TestConsoleProperties consoleProperties) {
    return new GocheckEventsConverter(consoleProperties);
  }
}
