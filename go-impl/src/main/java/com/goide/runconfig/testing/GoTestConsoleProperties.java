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

import com.goide.psi.GoFunctionOrMethodDeclaration;
import com.goide.runconfig.GoRunConfigurationBase;
import com.goide.sdk.GoPackageUtil;
import com.goide.util.GoUtil;
import consulo.execution.action.Location;
import consulo.execution.action.PsiLocation;
import consulo.execution.configuration.RunConfigurationBase;
import consulo.execution.configuration.RunProfile;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.test.AbstractTestProxy;
import consulo.execution.test.Filter;
import consulo.execution.test.TestConsoleProperties;
import consulo.execution.test.action.AbstractRerunFailedTestsAction;
import consulo.execution.test.sm.SMCustomMessagesParsing;
import consulo.execution.test.sm.runner.OutputToGeneralTestEventsConverter;
import consulo.execution.test.sm.runner.SMTRunnerConsoleProperties;
import consulo.execution.test.sm.runner.SMTestLocator;
import consulo.execution.ui.console.ConsoleView;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.scope.GlobalSearchScopesCore;
import consulo.module.Module;
import consulo.process.ExecutionException;
import consulo.project.Project;
import consulo.ui.ex.action.ActionManager;
import consulo.ui.ex.action.AnAction;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.util.VirtualFileUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;

public class GoTestConsoleProperties extends SMTRunnerConsoleProperties implements SMCustomMessagesParsing {
  public GoTestConsoleProperties(@Nonnull GoTestRunConfiguration configuration, @Nonnull Executor executor) {
    super(configuration, configuration.getTestFramework().getName(), executor);
    setPrintTestingStartedTime(false);
  }

  @Nonnull
  @Override
  protected GlobalSearchScope initScope() {
    RunProfile configuration = getConfiguration();
    if (configuration instanceof GoTestRunConfiguration) {
      Project project = ((GoTestRunConfiguration)configuration).getProject();
      Module module = ((GoTestRunConfiguration)configuration).getConfigurationModule().getModule();
      switch (((GoTestRunConfiguration)configuration).getKind()) {
        case DIRECTORY:
          String directoryUrl = VirtualFileUtil.pathToUrl(((GoTestRunConfiguration)configuration).getDirectoryPath());
          VirtualFile directory = VirtualFileManager.getInstance().findFileByUrl(directoryUrl);
          if (directory != null) {
            return GlobalSearchScopesCore.directoryScope(project, directory, true);
          }
          break;
        case PACKAGE:
          VirtualFile packageDir = GoPackageUtil.findByImportPath(((GoTestRunConfiguration)configuration).getPackage(), project, module);
          PsiDirectory psiDirectory = packageDir != null ? PsiManager.getInstance(project).findDirectory(packageDir) : null;
          if (psiDirectory != null) {
            return GoPackageUtil.packageScope(psiDirectory, null);
          }
          break;
        case FILE:
          String fileUrl = VirtualFileUtil.pathToUrl(((GoTestRunConfiguration)configuration).getFilePath());
          VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(fileUrl);
          if (file != null) {
            return GlobalSearchScope.fileScope(project, file);
          }
          break;
      }
    }
    if (configuration instanceof GoRunConfigurationBase) {
      GlobalSearchScope scope = GlobalSearchScope.EMPTY_SCOPE;
      for (Module module : ((GoRunConfigurationBase)configuration).getModules()) {
        scope = new GoUtil.TestsScope(GoUtil.goPathResolveScope(module, null));
      }
      return scope;
    }
    return super.initScope();
  }

  @Nullable
  @Override
  public SMTestLocator getTestLocator() {
    return GoTestLocator.INSTANCE;
  }

  @Nonnull
  @Override
  public OutputToGeneralTestEventsConverter createTestEventsConverter(@Nonnull String testFrameworkName,
                                                                      @Nonnull TestConsoleProperties consoleProperties) {
    RunProfile configuration = getConfiguration();
    assert configuration instanceof GoTestRunConfiguration;
    return ((GoTestRunConfiguration)configuration).createTestEventsConverter(consoleProperties);
  }

  @Nullable
  @Override
  public AbstractRerunFailedTestsAction createRerunFailedTestsAction(ConsoleView consoleView) {
    AnAction rerunFailedTestsAction = ActionManager.getInstance().getAction("RerunFailedTests");
    return rerunFailedTestsAction != null ? new GoRerunFailedTestsAction(this, consoleView) : null;
  }

  private static class GoRerunFailedTestsAction extends AbstractRerunFailedTestsAction {
    public GoRerunFailedTestsAction(GoTestConsoleProperties properties, ConsoleView view) {
      super(view);
      init(properties);
    }

    @Nonnull
    @Override
    protected Filter getFilter(@Nonnull Project project, @Nonnull GlobalSearchScope searchScope) {
      return super.getFilter(project, searchScope).and(new Filter() {
        @Override
        public boolean shouldAccept(AbstractTestProxy test) {
          Location location = test.getLocation(project, searchScope);
          return location instanceof PsiLocation && location.getPsiElement() instanceof GoFunctionOrMethodDeclaration;
        }
      });
    }

    @Nullable
    @Override
    protected MyRunProfile getRunProfile(@Nonnull ExecutionEnvironment environment) {
      return new MyRunProfile((RunConfigurationBase)myConsoleProperties.getConfiguration()) {
        @Nonnull
        @Override
        public Module[] getModules() {
          return Module.EMPTY_ARRAY;
        }

        @Nullable
        @Override
        public RunProfileState getState(@Nonnull Executor executor, @Nonnull ExecutionEnvironment environment) throws ExecutionException {
          RunConfigurationBase configurationBase = getPeer();
          if (configurationBase instanceof GoTestRunConfiguration) {
            List<AbstractTestProxy> failedTests = getFailedTests(configurationBase.getProject());
            if (failedTests.isEmpty()) {
              return null;
            }

            GoTestRunConfiguration goTestRunConfiguration = (GoTestRunConfiguration)configurationBase;
            Module module = goTestRunConfiguration.getConfigurationModule().getModule();
            GoTestRunningState runningState = goTestRunConfiguration.newRunningState(environment, module);
            runningState.setFailedTests(failedTests);
            return runningState;
          }
          return null;
        }
      };
    }
  }
}
