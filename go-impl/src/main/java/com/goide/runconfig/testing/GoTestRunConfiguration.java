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
import com.goide.runconfig.testing.ui.GoTestRunConfigurationEditorForm;
import com.goide.sdk.GoPackageUtil;
import consulo.execution.RuntimeConfigurationException;
import consulo.execution.configuration.ConfigurationType;
import consulo.execution.configuration.ModuleBasedConfiguration;
import consulo.execution.configuration.RunConfiguration;
import consulo.execution.configuration.RuntimeConfigurationError;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.test.TestConsoleProperties;
import consulo.execution.test.sm.runner.OutputToGeneralTestEventsConverter;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.module.Module;
import consulo.project.Project;
import consulo.util.io.FileUtil;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.InvalidDataException;
import consulo.util.xml.serializer.JDOMExternalizerUtil;
import consulo.util.xml.serializer.WriteExternalException;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import org.jdom.Element;

import jakarta.annotation.Nonnull;

public class GoTestRunConfiguration extends GoRunConfigurationBase<GoTestRunningState> {
  private static final String PATTERN_ATTRIBUTE_NAME = "pattern";
  private static final String FILE_PATH_ATTRIBUTE_NAME = "filePath";
  private static final String DIRECTORY_ATTRIBUTE_NAME = "directory";
  private static final String PACKAGE_ATTRIBUTE_NAME = "package";
  private static final String KIND_ATTRIBUTE_NAME = "kind";
  private static final String FRAMEWORK_ATTRIBUTE_NAME = "framework";

  @Nonnull
  private String myPackage = "";
  @Nonnull
  private String myFilePath = "";
  @Nonnull
  private String myDirectoryPath = "";

  @Nonnull
  private String myPattern = "";
  @Nonnull
  private Kind myKind = Kind.DIRECTORY;
  private GoTestFramework myTestFramework = GotestFramework.INSTANCE;

  public GoTestRunConfiguration(@Nonnull Project project, String name, @Nonnull ConfigurationType configurationType) {
    super(name, new GoModuleBasedConfiguration(project), configurationType.getConfigurationFactories()[0]);
  }

  public OutputToGeneralTestEventsConverter createTestEventsConverter(@Nonnull TestConsoleProperties consoleProperties) {
    return myTestFramework.createTestEventsConverter(consoleProperties);
  }

  @Nonnull
  @Override
  protected GoTestRunningState newRunningState(ExecutionEnvironment env, Module module) {
    return myTestFramework.newRunningState(env, module, this);
  }

  @Override
  protected ModuleBasedConfiguration createInstance() {
    return new GoTestRunConfiguration(getProject(), getName(), GoTestRunConfigurationType.getInstance());
  }

  @Nonnull
  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new GoTestRunConfigurationEditorForm(getProject());
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    super.checkConfiguration();
    GoModuleBasedConfiguration configurationModule = getConfigurationModule();
    if (!myTestFramework.isAvailable(configurationModule.getModule())) {
      throw new RuntimeConfigurationError("Framework `" + myTestFramework.getName() + "` is not available in selected module");
    }
    switch (myKind) {
      case DIRECTORY:
        String directoryPath = FileUtil.isAbsolutePlatformIndependent(myDirectoryPath)
                               ? myDirectoryPath
                               : FileUtil.join(getWorkingDirectory(), myDirectoryPath);

        if (!FileUtil.isAncestor(getWorkingDirectory(), directoryPath, false)) {
          throw new RuntimeConfigurationError("Working directory should be ancestor of testing directory");
        }
        VirtualFile testingDirectory = LocalFileSystem.getInstance().findFileByPath(directoryPath);
        if (testingDirectory == null) {
          throw new RuntimeConfigurationError("Testing directory doesn't exist");
        }
        break;
      case PACKAGE:
        Module module = configurationModule.getModule();
        assert module != null;

        VirtualFile packageDirectory = GoPackageUtil.findByImportPath(myPackage, module.getProject(), module);
        if (packageDirectory == null || !packageDirectory.isDirectory()) {
          throw new RuntimeConfigurationError("Cannot find package '" + myPackage + "'");
        }
        for (VirtualFile file : packageDirectory.getChildren()) {
          PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(file);
          if (psiFile != null && myTestFramework.isAvailableOnFile(psiFile)) {
            return;
          }
        }
        String message = "Cannot find Go test files in '" + myPackage + "' compatible with `" + myTestFramework.getName() + "` framework";
        throw new RuntimeConfigurationError(message);
      case FILE:
        VirtualFile virtualFile = findFile(getFilePath());
        if (virtualFile == null) {
          throw new RuntimeConfigurationError("Test file doesn't exist");
        }
        PsiFile file = PsiManager.getInstance(getProject()).findFile(virtualFile);
        if (file == null || !myTestFramework.isAvailableOnFile(file)) {
          message = "Framework `" + myTestFramework.getName() + "` is not available on file `" + myFilePath + "`";
          throw new RuntimeConfigurationError(message);
        }
        break;
    }
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);
    JDOMExternalizerUtil.addElementWithValueAttribute(element, FRAMEWORK_ATTRIBUTE_NAME, myTestFramework.getName());
    JDOMExternalizerUtil.addElementWithValueAttribute(element, KIND_ATTRIBUTE_NAME, myKind.name());
    if (!myPackage.isEmpty()) {
      JDOMExternalizerUtil.addElementWithValueAttribute(element, PACKAGE_ATTRIBUTE_NAME, myPackage);
    }
    if (!myDirectoryPath.isEmpty()) {
      JDOMExternalizerUtil.addElementWithValueAttribute(element, DIRECTORY_ATTRIBUTE_NAME, myDirectoryPath);
    }
    if (!myFilePath.isEmpty()) {
      JDOMExternalizerUtil.addElementWithValueAttribute(element, FILE_PATH_ATTRIBUTE_NAME, myFilePath);
    }
    if (!myPattern.isEmpty()) {
      JDOMExternalizerUtil.addElementWithValueAttribute(element, PATTERN_ATTRIBUTE_NAME, myPattern);
    }
  }

  @Override
  public void readExternal(@Nonnull Element element) throws InvalidDataException {
    super.readExternal(element);
    try {
      String kindName = JDOMExternalizerUtil.getFirstChildValueAttribute(element, KIND_ATTRIBUTE_NAME);
      myKind = kindName != null ? Kind.valueOf(kindName) : Kind.DIRECTORY;
    }
    catch (IllegalArgumentException e) {
      myKind = Kind.DIRECTORY;
    }
    myPackage = StringUtil.notNullize(JDOMExternalizerUtil.getFirstChildValueAttribute(element, PACKAGE_ATTRIBUTE_NAME));
    myDirectoryPath = StringUtil.notNullize(JDOMExternalizerUtil.getFirstChildValueAttribute(element, DIRECTORY_ATTRIBUTE_NAME));
    myFilePath = StringUtil.notNullize(JDOMExternalizerUtil.getFirstChildValueAttribute(element, FILE_PATH_ATTRIBUTE_NAME));
    myPattern = StringUtil.notNullize(JDOMExternalizerUtil.getFirstChildValueAttribute(element, PATTERN_ATTRIBUTE_NAME));
    myTestFramework = GoTestFramework.fromName(JDOMExternalizerUtil.getFirstChildValueAttribute(element, FRAMEWORK_ATTRIBUTE_NAME));
  }

  @Nonnull
  public String getPattern() {
    return myPattern;
  }

  public void setPattern(@Nonnull String pattern) {
    myPattern = pattern;
  }

  @Nonnull
  public Kind getKind() {
    return myKind;
  }

  public void setKind(@Nonnull Kind kind) {
    myKind = kind;
  }

  @Nonnull
  public String getPackage() {
    return myPackage;
  }

  public void setPackage(@Nonnull String aPackage) {
    myPackage = aPackage;
  }

  @Nonnull
  public String getFilePath() {
    return myFilePath;
  }

  public void setFilePath(@Nonnull String filePath) {
    myFilePath = filePath;
  }

  @Nonnull
  public String getDirectoryPath() {
    return myDirectoryPath;
  }

  public void setDirectoryPath(@Nonnull String directoryPath) {
    myDirectoryPath = directoryPath;
  }

  public void setTestFramework(@Nonnull GoTestFramework testFramework) {
    myTestFramework = testFramework;
  }

  @Nonnull
  public GoTestFramework getTestFramework() {
    return myTestFramework;
  }

  public enum Kind {
    DIRECTORY, PACKAGE, FILE
  }
}
