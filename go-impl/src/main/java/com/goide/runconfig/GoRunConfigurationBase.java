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

package com.goide.runconfig;

import com.goide.sdk.GoSdkService;
import com.goide.sdk.GoSdkUtil;
import consulo.execution.ExecutionBundle;
import consulo.execution.RuntimeConfigurationException;
import consulo.execution.RuntimeConfigurationWarning;
import consulo.execution.configuration.*;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.ui.awt.EnvironmentVariablesComponent;
import consulo.module.Module;
import consulo.process.ExecutionException;
import consulo.util.collection.ContainerUtil;
import consulo.util.io.FileUtil;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.InvalidDataException;
import consulo.util.xml.serializer.JDOMExternalizerUtil;
import consulo.util.xml.serializer.WriteExternalException;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import org.jdom.Element;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class GoRunConfigurationBase<RunningState extends GoRunningState>
  extends ModuleBasedConfiguration<GoModuleBasedConfiguration> implements RunConfigurationWithSuppressedDefaultRunAction {

  private static final String WORKING_DIRECTORY_NAME = "working_directory";
  private static final String GO_PARAMETERS_NAME = "go_parameters";
  private static final String PARAMETERS_NAME = "parameters";
  private static final String PASS_PARENT_ENV = "pass_parent_env";

  @Nonnull
  private String myWorkingDirectory = "";
  @Nonnull
  private String myGoParams = "";
  @Nonnull
  private String myParams = "";
  @Nonnull
  private final Map<String, String> myCustomEnvironment = new HashMap<>();
  private boolean myPassParentEnvironment = true;

  public GoRunConfigurationBase(String name, GoModuleBasedConfiguration configurationModule, ConfigurationFactory factory) {
    super(name, configurationModule, factory);

    Module module = configurationModule.getModule();
    if (module == null) {
      Collection<Module> modules = getValidModules();
      if (modules.size() == 1) {
        module = ContainerUtil.getFirstItem(modules);
        getConfigurationModule().setModule(module);
      }
    }

    if (module != null) {
      myWorkingDirectory = StringUtil.notNullize(module.getModuleDirPath());
    }
    else {
      myWorkingDirectory = StringUtil.notNullize(configurationModule.getProject().getBasePath());
    }
  }

  @Nullable
  @Override
  public RunProfileState getState(@Nonnull Executor executor, @Nonnull ExecutionEnvironment environment) throws ExecutionException {
    return createRunningState(environment);
  }

  @Nonnull
  @Override
  public Collection<Module> getValidModules() {
    return GoSdkUtil.getGoModules(getProject());
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    GoModuleBasedConfiguration configurationModule = getConfigurationModule();
    Module module = configurationModule.getModule();
    if (module != null) {
      if (GoSdkService.getInstance(module.getProject()).getSdkHomePath(module) == null) {
        throw new RuntimeConfigurationWarning("Go SDK is not specified for module '" + module.getName() + "'");
      }
    }
    else {
      String moduleName = configurationModule.getModuleName();
      if (moduleName != null) {
        throw new RuntimeConfigurationError(ExecutionBundle.message("module.doesn.t.exist.in.project.error.text", moduleName));
      }
      throw new RuntimeConfigurationError(ExecutionBundle.message("module.not.specified.error.text"));
    }
    if (myWorkingDirectory.isEmpty()) {
      throw new RuntimeConfigurationError("Working directory is not specified");
    }
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);
    writeModule(element);
    addNonEmptyElement(element, WORKING_DIRECTORY_NAME, myWorkingDirectory);
    addNonEmptyElement(element, GO_PARAMETERS_NAME, myGoParams);
    addNonEmptyElement(element, PARAMETERS_NAME, myParams);
    if (!myCustomEnvironment.isEmpty()) {
      EnvironmentVariablesComponent.writeExternal(element, myCustomEnvironment);
    }
    if (!myPassParentEnvironment) {
      JDOMExternalizerUtil.addElementWithValueAttribute(element, PASS_PARENT_ENV, "false");
    }
  }

  protected void addNonEmptyElement(@Nonnull Element element, @Nonnull String attributeName, @Nullable String value) {
    if (StringUtil.isNotEmpty(value)) {
      JDOMExternalizerUtil.addElementWithValueAttribute(element, attributeName, value);
    }
  }

  @Override
  public void readExternal(@Nonnull Element element) throws InvalidDataException {
    super.readExternal(element);
    readModule(element);
    myGoParams = StringUtil.notNullize(JDOMExternalizerUtil.getFirstChildValueAttribute(element, GO_PARAMETERS_NAME));
    myParams = StringUtil.notNullize(JDOMExternalizerUtil.getFirstChildValueAttribute(element, PARAMETERS_NAME));

    String workingDirectoryValue = JDOMExternalizerUtil.getFirstChildValueAttribute(element, WORKING_DIRECTORY_NAME);
    if (workingDirectoryValue != null) {
      myWorkingDirectory = workingDirectoryValue;
    }
    EnvironmentVariablesComponent.readExternal(element, myCustomEnvironment);

    String passEnvValue = JDOMExternalizerUtil.getFirstChildValueAttribute(element, PASS_PARENT_ENV);
    myPassParentEnvironment = passEnvValue == null || Boolean.valueOf(passEnvValue);
  }

  @Nonnull
  private RunningState createRunningState(ExecutionEnvironment env) throws ExecutionException {
    GoModuleBasedConfiguration configuration = getConfigurationModule();
    Module module = configuration.getModule();
    if (module == null) {
      throw new ExecutionException("Go isn't configured for run configuration: " + getName());
    }
    return newRunningState(env, module);
  }
  
  @Nullable
  protected VirtualFile findFile(@Nonnull String filePath) {
    VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(VirtualFileUtil.pathToUrl(filePath));
    if (virtualFile == null) {
      String path = FileUtil.join(getWorkingDirectory(), filePath);
      virtualFile = VirtualFileManager.getInstance().findFileByUrl(VirtualFileUtil.pathToUrl(path));
    }
    return virtualFile;
  }

  @Nonnull
  protected abstract RunningState newRunningState(ExecutionEnvironment env, Module module);

  @Nonnull
  public String getGoToolParams() {
    return myGoParams;
  }

  @Nonnull
  public String getParams() {
    return myParams;
  }

  public void setGoParams(@Nonnull String params) {
    myGoParams = params;
  }

  public void setParams(@Nonnull String params) {
    myParams = params;
  }

  @Nonnull
  public Map<String, String> getCustomEnvironment() {
    return myCustomEnvironment;
  }

  public void setCustomEnvironment(@Nonnull Map<String, String> customEnvironment) {
    myCustomEnvironment.clear();
    myCustomEnvironment.putAll(customEnvironment);
  }

  public void setPassParentEnvironment(boolean passParentEnvironment) {
    myPassParentEnvironment = passParentEnvironment;
  }

  public boolean isPassParentEnvironment() {
    return myPassParentEnvironment;
  }

  @Nonnull
  public String getWorkingDirectory() {
    return myWorkingDirectory;
  }

  @Nonnull
  public String getWorkingDirectoryUrl() {
    return VirtualFileUtil.pathToUrl(myWorkingDirectory);
  }

  public void setWorkingDirectory(@Nonnull String workingDirectory) {
    myWorkingDirectory = workingDirectory;
  }
}
