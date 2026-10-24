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

import com.goide.runconfig.GoModuleBasedConfiguration;
import com.goide.runconfig.GoRunConfigurationBase;
import com.goide.runconfig.testing.frameworks.gotest.GotestFramework;
import com.goide.util.GoPackageUtil;
import consulo.execution.RuntimeConfigurationException;
import consulo.execution.configuration.ConfigurationType;
import consulo.execution.configuration.LegacyRunConfigurationConverter;
import consulo.execution.configuration.ModuleBasedConfiguration;
import consulo.execution.configuration.RunConfigurationOptions;
import consulo.execution.configuration.RuntimeConfigurationError;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.test.TestConsoleProperties;
import consulo.execution.test.sm.runner.OutputToGeneralTestEventsConverter;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.module.Module;
import consulo.project.Project;
import consulo.util.io.FileUtil;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import org.jdom.Element;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class GoTestRunConfiguration extends GoRunConfigurationBase<GoTestRunningState> {
  private static final String PATTERN_ATTRIBUTE_NAME = "pattern";
  private static final String FILE_PATH_ATTRIBUTE_NAME = "filePath";
  private static final String DIRECTORY_ATTRIBUTE_NAME = "directory";
  private static final String PACKAGE_ATTRIBUTE_NAME = "package";
  private static final String KIND_ATTRIBUTE_NAME = "kind";
  private static final String FRAMEWORK_ATTRIBUTE_NAME = "framework";

  public GoTestRunConfiguration(Project project, String name, ConfigurationType configurationType) {
    super(name, new GoModuleBasedConfiguration(project), configurationType.getConfigurationFactories()[0]);
  }

  @Override
  protected Class<? extends RunConfigurationOptions> getOptionsClass() {
    return GoTestRunConfigurationOptions.class;
  }

  @Override
  public GoTestRunConfigurationOptions getOptions() {
    return (GoTestRunConfigurationOptions) super.getOptions();
  }

  @Override
  protected void convertLegacyState(LegacyRunConfigurationConverter converter) {
    super.convertLegacyState(converter);

    GoTestRunConfigurationOptions options = new GoTestRunConfigurationOptions();
    convertLegacyCommonState(converter, options);

    Element element = converter.getElement();
    Kind kind;
    try {
      String kindName = readLegacyValue(element, KIND_ATTRIBUTE_NAME);
      kind = kindName != null ? Kind.valueOf(kindName) : Kind.DIRECTORY;
    }
    catch (IllegalArgumentException e) {
      kind = Kind.DIRECTORY;
    }
    options.setKind(kind == Kind.DIRECTORY ? null : kind.name());
    options.setPackage(readLegacyValue(element, PACKAGE_ATTRIBUTE_NAME));
    options.setDirectoryPath(readLegacyValue(element, DIRECTORY_ATTRIBUTE_NAME));
    options.setFilePath(readLegacyValue(element, FILE_PATH_ATTRIBUTE_NAME));
    options.setPattern(readLegacyValue(element, PATTERN_ATTRIBUTE_NAME));

    GoTestFramework framework = GoTestFramework.fromName(readLegacyValue(element, FRAMEWORK_ATTRIBUTE_NAME));
    options.setFramework(framework == GotestFramework.INSTANCE ? null : framework.getName());

    converter.setOptions(options);
  }

  @Override
  public List<RunConfigurationFragment<?>> createFragments() {
    List<RunConfigurationFragment<?>> fragments = super.createFragments();
    fragments.add(0, new GoTestTargetFragment());
    return fragments;
  }

  public OutputToGeneralTestEventsConverter createTestEventsConverter(TestConsoleProperties consoleProperties) {
    return getTestFramework().createTestEventsConverter(consoleProperties);
  }

  @Override
  protected GoTestRunningState newRunningState(ExecutionEnvironment env, Module module) {
    return getTestFramework().newRunningState(env, module, this);
  }

  @Override
  protected ModuleBasedConfiguration createInstance() {
    return new GoTestRunConfiguration(getProject(), getName(), GoTestRunConfigurationType.getInstance());
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    super.checkConfiguration();
    GoTestFramework testFramework = getTestFramework();
    GoModuleBasedConfiguration configurationModule = getConfigurationModule();
    if (!testFramework.isAvailable(configurationModule.getModule())) {
      throw new RuntimeConfigurationError("Framework `" + testFramework.getName() + "` is not available in selected module");
    }
    switch (getKind()) {
      case DIRECTORY:
        String directoryPath = getDirectoryPath();
        String fullDirectoryPath = FileUtil.isAbsolutePlatformIndependent(directoryPath)
                                   ? directoryPath
                                   : FileUtil.join(getWorkingDirectory(), directoryPath);

        if (!FileUtil.isAncestor(getWorkingDirectory(), fullDirectoryPath, false)) {
          throw new RuntimeConfigurationError("Working directory should be ancestor of testing directory");
        }
        VirtualFile testingDirectory = LocalFileSystem.getInstance().findFileByPath(fullDirectoryPath);
        if (testingDirectory == null) {
          throw new RuntimeConfigurationError("Testing directory doesn't exist");
        }
        break;
      case PACKAGE:
        Module module = configurationModule.getModule();
        assert module != null;

        String aPackage = getPackage();
        VirtualFile packageDirectory = GoPackageUtil.findByImportPath(aPackage, module.getProject(), module);
        if (packageDirectory == null || !packageDirectory.isDirectory()) {
          throw new RuntimeConfigurationError("Cannot find package '" + aPackage + "'");
        }
        for (VirtualFile file : packageDirectory.getChildren()) {
          PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(file);
          if (psiFile != null && testFramework.isAvailableOnFile(psiFile)) {
            return;
          }
        }
        String message = "Cannot find Go test files in '" + aPackage + "' compatible with `" + testFramework.getName() + "` framework";
        throw new RuntimeConfigurationError(message);
      case FILE:
        VirtualFile virtualFile = findFile(getFilePath());
        if (virtualFile == null) {
          throw new RuntimeConfigurationError("Test file doesn't exist");
        }
        PsiFile file = PsiManager.getInstance(getProject()).findFile(virtualFile);
        if (file == null || !testFramework.isAvailableOnFile(file)) {
          message = "Framework `" + testFramework.getName() + "` is not available on file `" + getFilePath() + "`";
          throw new RuntimeConfigurationError(message);
        }
        break;
    }
  }

  public String getPattern() {
    return StringUtil.notNullize(getOptions().getPattern());
  }

  public void setPattern(@Nullable String pattern) {
    getOptions().setPattern(pattern);
  }

  public Kind getKind() {
    String kind = getOptions().getKind();
    if (kind == null) {
      return Kind.DIRECTORY;
    }
    try {
      return Kind.valueOf(kind);
    }
    catch (IllegalArgumentException e) {
      return Kind.DIRECTORY;
    }
  }

  public void setKind(@Nullable Kind kind) {
    getOptions().setKind(kind == null || kind == Kind.DIRECTORY ? null : kind.name());
  }

  public String getPackage() {
    return StringUtil.notNullize(getOptions().getPackage());
  }

  public void setPackage(@Nullable String aPackage) {
    getOptions().setPackage(aPackage);
  }

  public String getFilePath() {
    return StringUtil.notNullize(getOptions().getFilePath());
  }

  public void setFilePath(@Nullable String filePath) {
    getOptions().setFilePath(filePath);
  }

  public String getDirectoryPath() {
    return StringUtil.notNullize(getOptions().getDirectoryPath());
  }

  public void setDirectoryPath(@Nullable String directoryPath) {
    getOptions().setDirectoryPath(directoryPath);
  }

  public void setTestFramework(GoTestFramework testFramework) {
    getOptions().setFramework(testFramework == GotestFramework.INSTANCE ? null : testFramework.getName());
  }

  public GoTestFramework getTestFramework() {
    return GoTestFramework.fromName(getOptions().getFramework());
  }

  public enum Kind {
    DIRECTORY, PACKAGE, FILE
  }
}
