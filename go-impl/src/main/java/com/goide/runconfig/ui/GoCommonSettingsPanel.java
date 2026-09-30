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

package com.goide.runconfig.ui;

import com.goide.runconfig.GoRunConfigurationBase;
import com.goide.runconfig.GoRunUtil;
import consulo.execution.localize.ExecutionLocalize;
import consulo.execution.ui.awt.EnvironmentVariablesTextFieldWithBrowseButton;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.google.go.localize.GoLocalize;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.process.cmd.ParametersListUtil;
import consulo.project.Project;
import consulo.ui.ComboBox;
import consulo.ui.Component;
import consulo.ui.TextBoxWithExpandAction;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.model.FlatDataModel;
import consulo.ui.model.MutableFlatDataModel;
import consulo.ui.util.FormBuilder;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GoCommonSettingsPanel {
  protected final Project myProject;

  private final MutableFlatDataModel<Module> myModules = FlatDataModel.of(new ArrayList<>());

  private final FileChooserTextBoxBuilder.Controller myWorkingDirectoryField;
  private final EnvironmentVariablesTextFieldWithBrowseButton myEnvironmentField;
  private final TextBoxWithExpandAction myGoToolParamsField;
  private final TextBoxWithExpandAction myParamsField;
  private final ComboBox<Module> myModulesComboBox;

  @RequiredUIAccess
  public GoCommonSettingsPanel(Project project) {
    myProject = project;

    myWorkingDirectoryField = FileChooserTextBoxBuilder.create(project)
      .fileChooserDescriptor(GoRunUtil.createFileChooserDescriptor(project, true, false, null))
      .build();

    myEnvironmentField = new EnvironmentVariablesTextFieldWithBrowseButton();

    myGoToolParamsField = createParametersField(GoLocalize.goRunConfigurationGoToolArgumentsTitle());
    myParamsField = createParametersField(GoLocalize.goRunConfigurationProgramArgumentsTitle());

    myModulesComboBox = ComboBox.create(myModules);
    myModulesComboBox.setRender((presentation, item) -> {
      Module module = item.getValue();
      if (module != null) {
        presentation.withIcon(PlatformIconGroup.nodesModule());
        presentation.append(module.getName());
      }
    });
  }

  @RequiredUIAccess
  private static TextBoxWithExpandAction createParametersField(LocalizeValue dialogTitle) {
    return TextBoxWithExpandAction.create(
      PlatformIconGroup.actionsShow(),
      dialogTitle.get(),
      ParametersListUtil.DEFAULT_LINE_PARSER,
      ParametersListUtil.DEFAULT_LINE_JOINER
    );
  }

  @RequiredUIAccess
  public Component build() {
    FormBuilder builder = FormBuilder.create();

    addBefore(builder);

    builder.addLabeled(ExecutionLocalize.runConfigurationWorkingDirectoryLabel(), myWorkingDirectoryField.getComponent());
    builder.addLabeled(
      LocalizeValue.join(ExecutionLocalize.environmentVariablesComponentTitle(), LocalizeValue.colon()),
      myEnvironmentField.getComponent()
    );
    builder.addLabeled(GoLocalize.goRunConfigurationGoToolArgumentsLabel(), myGoToolParamsField);
    builder.addLabeled(ExecutionLocalize.runConfigurationProgramParameters(), myParamsField);
    builder.addLabeled(GoLocalize.goRunConfigurationModuleLabel(), myModulesComboBox);

    addAfter(builder);

    return builder.build();
  }

  @RequiredUIAccess
  protected void addBefore(FormBuilder builder) {
  }

  @RequiredUIAccess
  protected void addAfter(FormBuilder builder) {
  }

  @RequiredUIAccess
  public void resetEditorFrom(GoRunConfigurationBase<?> configuration) {
    List<Module> modules = new ArrayList<>(configuration.getValidModules());
    Module module = configuration.getConfigurationModule().getModule();
    if (module != null && !modules.contains(module)) {
      modules.add(module);
    }
    myModules.replaceAll(modules);
    myModulesComboBox.setValue(module);
    myGoToolParamsField.setValue(StringUtil.notNullize(configuration.getGoToolParams()));
    myParamsField.setValue(StringUtil.notNullize(configuration.getParams()));
    myWorkingDirectoryField.setValue(StringUtil.notNullize(configuration.getWorkingDirectory()));
    myEnvironmentField.setEnvs(configuration.getCustomEnvironment());
    myEnvironmentField.setPassParentEnvs(configuration.isPassParentEnvironment());
  }

  @RequiredUIAccess
  public void applyEditorTo(GoRunConfigurationBase<?> configuration) {
    configuration.setModule(myModulesComboBox.getValue());
    configuration.setGoParams(StringUtil.notNullize(myGoToolParamsField.getValue()));
    configuration.setParams(StringUtil.notNullize(myParamsField.getValue()));
    configuration.setWorkingDirectory(StringUtil.notNullize(myWorkingDirectoryField.getValue()));
    configuration.setCustomEnvironment(myEnvironmentField.getEnvs());
    configuration.setPassParentEnvironment(myEnvironmentField.isPassParentEnvs());
  }

  public @Nullable Module getSelectedModule() {
    return myModulesComboBox.getValue();
  }
}
