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

package com.goide.runconfig.ui;

import com.goide.runconfig.GoRunUtil;
import com.goide.runconfig.application.GoApplicationConfiguration;
import com.goide.runconfig.testing.ui.GoPackageFieldCompletionProvider;
import consulo.configurable.ConfigurationException;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.google.go.localize.GoLocalize;
import consulo.language.editor.ui.EditorBox;
import consulo.language.editor.ui.EditorBoxBuilderFactory;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.ComboBox;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.Nullable;

public class GoApplicationConfigurationEditorForm extends SettingsEditor<GoApplicationConfiguration> {
  private final Project myProject;
  private @Nullable Panel myPanel;

  public GoApplicationConfigurationEditorForm(Project project) {
    myProject = project;
  }

  @RequiredUIAccess
  @Override
  protected Component createUIComponent() {
    Panel panel = new Panel();
    myPanel = panel;
    Component component = panel.build();
    panel.onRunKindChanged();
    return component;
  }

  @RequiredUIAccess
  @Override
  protected void resetEditorFrom(GoApplicationConfiguration configuration) {
    Panel panel = myPanel;
    if (panel != null) {
      panel.reset(configuration);
    }
  }

  @RequiredUIAccess
  @Override
  protected void applyEditorTo(GoApplicationConfiguration configuration) throws ConfigurationException {
    Panel panel = myPanel;
    if (panel != null) {
      panel.apply(configuration);
    }
  }

  private static LocalizeValue getKindName(GoApplicationConfiguration.Kind kind) {
    return switch (kind) {
      case PACKAGE -> GoLocalize.goRunConfigurationKindPackage();
      case FILE -> GoLocalize.goRunConfigurationKindFile();
    };
  }

  private class Panel extends GoCommonSettingsPanel {
    private final ComboBox<GoApplicationConfiguration.Kind> myRunKindComboBox;
    private final Label myPackageLabel;
    private final EditorBox myPackageField;
    private final Label myFileLabel;
    private final FileChooserTextBoxBuilder.Controller myFileField;
    private final FileChooserTextBoxBuilder.Controller myOutputFilePathField;

    @RequiredUIAccess
    private Panel() {
      super(GoApplicationConfigurationEditorForm.this.myProject);

      myRunKindComboBox = ComboBox.create(GoApplicationConfiguration.Kind.values());
      myRunKindComboBox.setTextRenderer(kind -> kind == null ? LocalizeValue.empty() : getKindName(kind));
      myRunKindComboBox.addValueListener(event -> onRunKindChanged());

      myPackageLabel = Label.create(GoLocalize.goRunConfigurationPackageLabel());
      myPackageField = myProject.getApplication()
        .getInstance(EditorBoxBuilderFactory.class)
        .create(myProject)
        .completion(new GoPackageFieldCompletionProvider(this::getSelectedModule))
        .build();

      myFileLabel = Label.create(GoLocalize.goRunConfigurationFileLabel());
      myFileField = FileChooserTextBoxBuilder.create(myProject)
        .fileChooserDescriptor(GoRunUtil.createGoWithMainFileChooserDescriptor(myProject))
        .build();

      myOutputFilePathField = FileChooserTextBoxBuilder.create(myProject)
        .fileChooserDescriptor(GoRunUtil.createFileChooserDescriptor(myProject, true, true, null))
        .build();
    }

    @RequiredUIAccess
    @Override
    protected void addBefore(FormBuilder builder) {
      builder.addLabeled(GoLocalize.goRunConfigurationRunKindLabel(), myRunKindComboBox);
      builder.addLabeled(myPackageLabel, myPackageField);
      builder.addLabeled(myFileLabel, myFileField.getComponent());
      builder.addLabeled(GoLocalize.goRunConfigurationOutputDirectoryLabel(), myOutputFilePathField.getComponent());
    }

    @RequiredUIAccess
    private void onRunKindChanged() {
      GoApplicationConfiguration.Kind selectedKind = myRunKindComboBox.getValue();
      if (selectedKind == null) {
        selectedKind = GoApplicationConfiguration.Kind.PACKAGE;
      }
      boolean thePackage = selectedKind == GoApplicationConfiguration.Kind.PACKAGE;
      boolean file = selectedKind == GoApplicationConfiguration.Kind.FILE;

      myPackageLabel.setVisible(thePackage);
      myPackageField.setVisible(thePackage);
      myFileLabel.setVisible(file);
      myFileField.getComponent().setVisible(file);
    }

    @RequiredUIAccess
    private void reset(GoApplicationConfiguration configuration) {
      myFileField.setValue(StringUtil.notNullize(configuration.getFilePath()));
      myPackageField.setValue(StringUtil.notNullize(configuration.getPackage()));
      myRunKindComboBox.setValue(configuration.getKind());
      myOutputFilePathField.setValue(StringUtil.notNullize(configuration.getOutputFilePath()));
      resetEditorFrom(configuration);
      onRunKindChanged();
    }

    @RequiredUIAccess
    private void apply(GoApplicationConfiguration configuration) {
      configuration.setFilePath(StringUtil.notNullize(myFileField.getValue()));
      configuration.setPackage(StringUtil.notNullize(myPackageField.getValue()));
      configuration.setKind(myRunKindComboBox.getValue());
      configuration.setFileOutputPath(StringUtil.nullize(myOutputFilePathField.getValue()));
      applyEditorTo(configuration);
    }
  }
}
