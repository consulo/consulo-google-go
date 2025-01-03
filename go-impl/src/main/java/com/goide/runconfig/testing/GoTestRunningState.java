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

package com.goide.runconfig.testing;

import com.goide.psi.GoFile;
import com.goide.psi.GoFunctionDeclaration;
import com.goide.runconfig.GoConsoleFilter;
import com.goide.runconfig.GoRunningState;
import com.goide.util.GoExecutor;
import consulo.execution.DefaultExecutionResult;
import consulo.execution.ExecutionResult;
import consulo.execution.executor.Executor;
import consulo.execution.process.ProcessTerminatedListener;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.runner.ProgramRunner;
import consulo.execution.test.AbstractTestProxy;
import consulo.execution.test.action.AbstractRerunFailedTestsAction;
import consulo.execution.test.action.ToggleAutoTestAction;
import consulo.execution.test.sm.SMTestRunnerConnectionUtil;
import consulo.execution.test.sm.ui.SMTRunnerConsoleView;
import consulo.execution.ui.console.ConsoleView;
import consulo.execution.ui.console.TextConsoleBuilder;
import consulo.execution.ui.console.TextConsoleBuilderFactory;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.module.Module;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.util.collection.ContainerUtil;
import consulo.util.io.FileUtil;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public class GoTestRunningState extends GoRunningState<GoTestRunConfiguration> {
  private String myCoverageFilePath;
  private String myFailedTestsPattern;

  public GoTestRunningState(@Nonnull ExecutionEnvironment env, @Nonnull Module module, @Nonnull GoTestRunConfiguration configuration) {
    super(env, module, configuration);
  }

  @Nonnull
  @Override
  public ExecutionResult execute(@Nonnull Executor executor, @Nonnull ProgramRunner runner) throws ExecutionException {
    ProcessHandler processHandler = startProcess();
    TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(myConfiguration.getProject());
    setConsoleBuilder(consoleBuilder);

    GoTestConsoleProperties consoleProperties = new GoTestConsoleProperties(myConfiguration, executor);
    String frameworkName = myConfiguration.getTestFramework().getName();
    ConsoleView consoleView = SMTestRunnerConnectionUtil.createAndAttachConsole(frameworkName, processHandler, consoleProperties);
    consoleView.addMessageFilter(new GoConsoleFilter(myConfiguration.getProject(), myModule, myConfiguration.getWorkingDirectoryUrl()));
    ProcessTerminatedListener.attach(processHandler);

    DefaultExecutionResult executionResult = new DefaultExecutionResult(consoleView, processHandler);
    AbstractRerunFailedTestsAction rerunFailedTestsAction = consoleProperties.createRerunFailedTestsAction(consoleView);
    if (rerunFailedTestsAction != null) {
      rerunFailedTestsAction.setModelProvider(((SMTRunnerConsoleView)consoleView)::getResultsViewer);
      executionResult.setRestartActions(rerunFailedTestsAction, new ToggleAutoTestAction());
    }
    else {
      executionResult.setRestartActions(new ToggleAutoTestAction());
    }
    return executionResult;
  }

  @Override
  protected GoExecutor patchExecutor(@Nonnull GoExecutor executor) throws ExecutionException {
    executor.withParameters("test", "-v");
    executor.withParameterString(myConfiguration.getGoToolParams());
    switch (myConfiguration.getKind()) {
      case DIRECTORY:
        String relativePath = FileUtil.getRelativePath(myConfiguration.getWorkingDirectory(),
                                                       myConfiguration.getDirectoryPath(),
                                                       File.separatorChar);
        // TODO Once Go gets support for covering multiple packages the ternary condition should be reverted
        // See https://golang.org/issues/6909
        String pathSuffix = myCoverageFilePath == null ? "..." : ".";
        if (relativePath != null && !".".equals(relativePath)) {
          executor.withParameters("./" + relativePath + "/" + pathSuffix);
        }
        else {
          executor.withParameters("./" + pathSuffix);
          executor.withWorkDirectory(myConfiguration.getDirectoryPath());
        }
        addFilterParameter(executor, ObjectUtil.notNull(myFailedTestsPattern, myConfiguration.getPattern()));
        break;
      case PACKAGE:
        executor.withParameters(myConfiguration.getPackage());
        addFilterParameter(executor, ObjectUtil.notNull(myFailedTestsPattern, myConfiguration.getPattern()));
        break;
      case FILE:
        String filePath = myConfiguration.getFilePath();
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath);
        if (virtualFile == null) {
          throw new ExecutionException("Test file doesn't exist");
        }
        PsiFile file = PsiManager.getInstance(myConfiguration.getProject()).findFile(virtualFile);
        if (file == null || !GoTestFinder.isTestFile(file)) {
          throw new ExecutionException("File '" + filePath + "' is not test file");
        }

        String importPath = ((GoFile)file).getImportPath(false);
        if (StringUtil.isEmpty(importPath)) {
          throw new ExecutionException("Cannot find import path for " + filePath);
        }

        executor.withParameters(importPath);
        addFilterParameter(executor, myFailedTestsPattern != null ? myFailedTestsPattern : buildFilterPatternForFile((GoFile)file));
        break;
    }

    if (myCoverageFilePath != null) {
      executor.withParameters("-coverprofile=" + myCoverageFilePath, "-covermode=atomic");
    }

    return executor;
  }

  @Nonnull
  protected String buildFilterPatternForFile(GoFile file) {
    Collection<String> testNames = new LinkedHashSet<>();
    for (GoFunctionDeclaration function : file.getFunctions()) {
      ContainerUtil.addIfNotNull(testNames, GoTestFinder.isTestOrExampleFunction(function) ? function.getName() : null);
    }
    return "^" + StringUtil.join(testNames, "|") + "$";
  }

  protected void addFilterParameter(@Nonnull GoExecutor executor, String pattern) {
    if (StringUtil.isNotEmpty(pattern)) {
      executor.withParameters("-run", pattern);
    }
  }

  public void setCoverageFilePath(@Nullable String coverageFile) {
    myCoverageFilePath = coverageFile;
  }

  public void setFailedTests(@Nonnull List<AbstractTestProxy> failedTests) {
    myFailedTestsPattern = "^" + StringUtil.join(failedTests, AbstractTestProxy::getName, "|") + "$";
  }
}
