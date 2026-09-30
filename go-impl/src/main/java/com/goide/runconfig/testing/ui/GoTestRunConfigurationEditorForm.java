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

package com.goide.runconfig.testing.ui;

import com.goide.runconfig.GoRunUtil;
import com.goide.runconfig.testing.GoTestFramework;
import com.goide.runconfig.testing.GoTestRunConfiguration;
import com.goide.runconfig.testing.frameworks.gobench.GobenchFramework;
import com.goide.runconfig.testing.frameworks.gocheck.GocheckFramework;
import com.goide.runconfig.testing.frameworks.gotest.GotestFramework;
import com.goide.runconfig.ui.GoCommonSettingsPanel;
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
import consulo.ui.RadioGroup;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.HorizontalLayout;
import consulo.ui.util.FormBuilder;
import consulo.util.lang.StringUtil;
import org.intellij.lang.regexp.RegExpLanguage;
import org.jspecify.annotations.Nullable;

public class GoTestRunConfigurationEditorForm extends SettingsEditor<GoTestRunConfiguration> {
  private final Project myProject;
  private @Nullable Panel myPanel;

  public GoTestRunConfigurationEditorForm(Project project) {
    myProject = project;
  }

  @RequiredUIAccess
  @Override
  protected Component createUIComponent() {
    Panel panel = new Panel();
    myPanel = panel;
    Component component = panel.build();
    panel.onTestKindChanged();
    return component;
  }

  @RequiredUIAccess
  @Override
  protected void resetEditorFrom(GoTestRunConfiguration configuration) {
    Panel panel = myPanel;
    if (panel != null) {
      panel.reset(configuration);
    }
  }

  @RequiredUIAccess
  @Override
  protected void applyEditorTo(GoTestRunConfiguration configuration) throws ConfigurationException {
    Panel panel = myPanel;
    if (panel != null) {
      panel.apply(configuration);
    }
  }

  private static LocalizeValue getKindName(GoTestRunConfiguration.Kind kind) {
    return switch (kind) {
      case DIRECTORY -> GoLocalize.goRunConfigurationKindDirectory();
      case PACKAGE -> GoLocalize.goRunConfigurationKindPackage();
      case FILE -> GoLocalize.goRunConfigurationKindFile();
    };
  }

  private class Panel extends GoCommonSettingsPanel {
    private final RadioGroup<GoTestFramework> myFrameworkGroup;
    private final HorizontalLayout myFrameworksLayout;
    private final ComboBox<GoTestRunConfiguration.Kind> myTestKindComboBox;
    private final Label myDirectoryLabel;
    private final FileChooserTextBoxBuilder.Controller myDirectoryField;
    private final Label myPackageLabel;
    private final EditorBox myPackageField;
    private final Label myFileLabel;
    private final FileChooserTextBoxBuilder.Controller myFileField;
    private final Label myPatternLabel;
    private final EditorBox myPatternEditor;

    @RequiredUIAccess
    private Panel() {
      super(GoTestRunConfigurationEditorForm.this.myProject);

      myFrameworkGroup = RadioGroup.create();
      myFrameworksLayout = HorizontalLayout.create();
      for (GoTestFramework framework : new GoTestFramework[]{GotestFramework.INSTANCE, GocheckFramework.INSTANCE, GobenchFramework.INSTANCE}) {
        myFrameworksLayout.add(myFrameworkGroup.newButton(LocalizeValue.of(framework.getName()), framework));
      }

      myTestKindComboBox = ComboBox.create(GoTestRunConfiguration.Kind.values());
      myTestKindComboBox.setTextRenderer(kind -> kind == null ? LocalizeValue.empty() : getKindName(kind));
      myTestKindComboBox.addValueListener(event -> onTestKindChanged());

      myDirectoryLabel = Label.create(GoLocalize.goRunConfigurationDirectoryLabel());
      myDirectoryField = FileChooserTextBoxBuilder.create(myProject)
        .fileChooserDescriptor(GoRunUtil.createFileChooserDescriptor(myProject, true, false, null))
        .build();

      myPackageLabel = Label.create(GoLocalize.goRunConfigurationPackageLabel());
      myPackageField = myProject.getApplication()
        .getInstance(EditorBoxBuilderFactory.class)
        .create(myProject)
        .completion(new GoPackageFieldCompletionProvider(this::getSelectedModule))
        .build();

      myFileLabel = Label.create(GoLocalize.goRunConfigurationFileLabel());
      myFileField = FileChooserTextBoxBuilder.create(myProject)
        .fileChooserDescriptor(GoRunUtil.createFileChooserDescriptor(myProject, false, false, null))
        .build();

      myPatternLabel = Label.create(GoLocalize.goRunConfigurationPatternLabel());
      myPatternEditor = myProject.getApplication()
        .getInstance(EditorBoxBuilderFactory.class)
        .create(myProject)
        .language(RegExpLanguage.INSTANCE)
        .build();
    }

    @RequiredUIAccess
    @Override
    protected void addBefore(FormBuilder builder) {
      builder.addLabeled(GoLocalize.goRunConfigurationTestFrameworkLabel(), myFrameworksLayout);
      builder.addLabeled(GoLocalize.goRunConfigurationTestKindLabel(), myTestKindComboBox);
      builder.addLabeled(myDirectoryLabel, myDirectoryField.getComponent());
      builder.addLabeled(myPackageLabel, myPackageField);
      builder.addLabeled(myFileLabel, myFileField.getComponent());
      builder.addLabeled(myPatternLabel, myPatternEditor);
    }

    @RequiredUIAccess
    private void onTestKindChanged() {
      GoTestRunConfiguration.Kind selectedKind = myTestKindComboBox.getValue();
      if (selectedKind == null) {
        selectedKind = GoTestRunConfiguration.Kind.DIRECTORY;
      }
      boolean allInPackage = selectedKind == GoTestRunConfiguration.Kind.PACKAGE;
      boolean allInDirectory = selectedKind == GoTestRunConfiguration.Kind.DIRECTORY;
      boolean file = selectedKind == GoTestRunConfiguration.Kind.FILE;

      myPackageLabel.setVisible(allInPackage);
      myPackageField.setVisible(allInPackage);
      myDirectoryLabel.setVisible(allInDirectory);
      myDirectoryField.getComponent().setVisible(allInDirectory);
      myFileLabel.setVisible(file);
      myFileField.getComponent().setVisible(file);
      myPatternLabel.setVisible(!file);
      myPatternEditor.setVisible(!file);
    }

    @RequiredUIAccess
    private void reset(GoTestRunConfiguration configuration) {
      myFrameworkGroup.setValue(configuration.getTestFramework(), false);
      myTestKindComboBox.setValue(configuration.getKind());
      myPackageField.setValue(StringUtil.notNullize(configuration.getPackage()));

      String basePath = StringUtil.notNullize(configuration.getProject().getBasePath());

      String directoryPath = configuration.getDirectoryPath();
      myDirectoryField.setValue(directoryPath.isEmpty() ? basePath : directoryPath);

      String filePath = configuration.getFilePath();
      myFileField.setValue(filePath.isEmpty() ? basePath : filePath);

      myPatternEditor.setValue(StringUtil.notNullize(configuration.getPattern()));

      resetEditorFrom(configuration);
      onTestKindChanged();
    }

    @RequiredUIAccess
    private void apply(GoTestRunConfiguration configuration) {
      GoTestFramework framework = myFrameworkGroup.getValue();
      configuration.setTestFramework(framework == null ? GotestFramework.INSTANCE : framework);
      configuration.setKind(myTestKindComboBox.getValue());
      configuration.setPackage(StringUtil.notNullize(myPackageField.getValue()));
      configuration.setDirectoryPath(StringUtil.notNullize(myDirectoryField.getValue()));
      configuration.setFilePath(StringUtil.notNullize(myFileField.getValue()));
      configuration.setPattern(StringUtil.notNullize(myPatternEditor.getValue()));

      applyEditorTo(configuration);
    }
  }
}
