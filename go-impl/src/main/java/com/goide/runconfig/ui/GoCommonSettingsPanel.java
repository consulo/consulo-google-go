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
import consulo.execution.ui.awt.EnvironmentVariablesTextFieldWithBrowseButton;
import consulo.execution.ui.awt.RawCommandLineEditor;
import consulo.module.Module;
import consulo.module.ui.awt.ModuleListCellRenderer;
import consulo.project.Project;
import consulo.ui.ex.awt.ComboBox;
import consulo.ui.ex.awt.FormBuilder;
import consulo.ui.ex.awt.MutableCollectionComboBoxModel;
import consulo.ui.ex.awt.TextFieldWithBrowseButton;
import consulo.ui.ex.awtUnsafe.TargetAWT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GoCommonSettingsPanel extends JPanel {
  private RawCommandLineEditor myGoToolParamsField;
  private RawCommandLineEditor myParamsField;
  private TextFieldWithBrowseButton myWorkingDirectoryField;
  private EnvironmentVariablesTextFieldWithBrowseButton myEnvironmentField;
  private ComboBox<Module> myModulesComboBox;

  public GoCommonSettingsPanel() {
    super(new BorderLayout());

    FormBuilder builder = FormBuilder.createFormBuilder();

    addBefore(builder);

    myWorkingDirectoryField = new TextFieldWithBrowseButton();
    builder.addLabeledComponent("&Working directory:", myWorkingDirectoryField);
    myEnvironmentField = new EnvironmentVariablesTextFieldWithBrowseButton();
    builder.addLabeledComponent("&Environment:", (JComponent) TargetAWT.to(myEnvironmentField.getComponent()));
    myGoToolParamsField = new RawCommandLineEditor();
    builder.addLabeledComponent("&Go tool arguments:", myGoToolParamsField);
    myParamsField = new RawCommandLineEditor();
    builder.addLabeledComponent("Pr&ogram arguments:", myParamsField);
    myModulesComboBox = new ComboBox<>();
    builder.addLabeledComponent("&Module:", myModulesComboBox);

    addAfter(builder);
    
    add(builder.getPanel(), BorderLayout.CENTER);
  }

  protected void addBefore(FormBuilder builder) {
  }

  protected void addAfter(FormBuilder builder) {
  }

  public void init(@Nonnull Project project) {
    GoRunUtil.installFileChooser(project, myWorkingDirectoryField, true);
    myGoToolParamsField.setDialogCaption("Go tool arguments");
    myParamsField.setDialogCaption("Program arguments");
    myModulesComboBox.setRenderer(new ModuleListCellRenderer());
  }

  public void resetEditorFrom(@Nonnull GoRunConfigurationBase<?> configuration) {
    myModulesComboBox.setModel(new MutableCollectionComboBoxModel<>(new ArrayList<>(configuration.getValidModules())));
    myModulesComboBox.setSelectedItem(configuration.getConfigurationModule().getModule());
    myGoToolParamsField.setText(configuration.getGoToolParams());
    myParamsField.setText(configuration.getParams());
    myWorkingDirectoryField.setText(configuration.getWorkingDirectory());
    myEnvironmentField.setEnvs(configuration.getCustomEnvironment());
    myEnvironmentField.setPassParentEnvs(configuration.isPassParentEnvironment());
  }

  public void applyEditorTo(@Nonnull GoRunConfigurationBase<?> configuration) {
    configuration.setModule((Module) myModulesComboBox.getSelectedItem());
    configuration.setGoParams(myGoToolParamsField.getText());
    configuration.setParams(myParamsField.getText());
    configuration.setWorkingDirectory(myWorkingDirectoryField.getText());
    configuration.setCustomEnvironment(myEnvironmentField.getEnvs());
    configuration.setPassParentEnvironment(myEnvironmentField.isPassParentEnvs());
  }

  @Nullable
  public Module getSelectedModule() {
    return (Module) myModulesComboBox.getSelectedItem();
  }
}
