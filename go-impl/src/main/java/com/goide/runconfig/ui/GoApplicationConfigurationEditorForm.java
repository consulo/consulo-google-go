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
import consulo.language.editor.ui.awt.EditorTextField;
import consulo.project.Project;
import consulo.ui.ex.awt.*;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import javax.swing.*;
import java.util.Locale;

public class GoApplicationConfigurationEditorForm extends SettingsEditor<GoApplicationConfiguration> {
  @Nonnull
  private final Project myProject;
  private TextFieldWithBrowseButton myFileField;
  private GoCommonSettingsPanel myCommonSettingsPanel;
  private EditorTextField myPackageField;
  private JComboBox myRunKindComboBox;
  private JLabel myPackageLabel;
  private JLabel myFileLabel;
  private TextFieldWithBrowseButton myOutputFilePathField;

  public GoApplicationConfigurationEditorForm(@Nonnull Project project) {
    super(null);
    myCommonSettingsPanel = new GoCommonSettingsPanel() {
      @Override
      protected void addBefore(FormBuilder builder) {
        builder.addLabeledComponent("&Run kind", myRunKindComboBox = new ComboBox());
        myPackageField = new GoPackageFieldCompletionProvider(this::getSelectedModule).createEditor(project);
        builder.addLabeledComponent(myPackageLabel = new JBLabel("Package"), myPackageField);
        builder.addLabeledComponent(myFileLabel = new JBLabel("File"), myFileField = new TextFieldWithBrowseButton());
        builder.addLabeledComponent("O&utput directory", myOutputFilePathField = new TextFieldWithBrowseButton());
      }
    };
    myProject = project;
    myCommonSettingsPanel.init(project);

    installRunKindComboBox();
    GoRunUtil.installGoWithMainFileChooser(myProject, myFileField);
    GoRunUtil.installFileChooser(myProject, myOutputFilePathField, true, true);
  }

  private void onRunKindChanged() {
    GoApplicationConfiguration.Kind selectedKind = (GoApplicationConfiguration.Kind) myRunKindComboBox.getSelectedItem();
    if (selectedKind == null) {
      selectedKind = GoApplicationConfiguration.Kind.PACKAGE;
    }
    boolean thePackage = selectedKind == GoApplicationConfiguration.Kind.PACKAGE;
    boolean file = selectedKind == GoApplicationConfiguration.Kind.FILE;

    myPackageField.setVisible(thePackage);
    myPackageLabel.setVisible(thePackage);
    myFileField.setVisible(file);
    myFileLabel.setVisible(file);
  }

  @Override
  protected void resetEditorFrom(@Nonnull GoApplicationConfiguration configuration) {
    myFileField.setText(configuration.getFilePath());
    myPackageField.setText(configuration.getPackage());
    myRunKindComboBox.setSelectedItem(configuration.getKind());
    myOutputFilePathField.setText(StringUtil.notNullize(configuration.getOutputFilePath()));
    myCommonSettingsPanel.resetEditorFrom(configuration);
  }

  @Override
  protected void applyEditorTo(@Nonnull GoApplicationConfiguration configuration) throws ConfigurationException {
    configuration.setFilePath(myFileField.getText());
    configuration.setPackage(myPackageField.getText());
    configuration.setKind((GoApplicationConfiguration.Kind) myRunKindComboBox.getSelectedItem());
    configuration.setFileOutputPath(StringUtil.nullize(myOutputFilePathField.getText()));
    myCommonSettingsPanel.applyEditorTo(configuration);
  }

  private void createUIComponents() {

  }

  @Nullable
  private static ListCellRendererWrapper<GoApplicationConfiguration.Kind> getRunKindListCellRendererWrapper() {
    return new ListCellRendererWrapper<GoApplicationConfiguration.Kind>() {
      @Override
      public void customize(JList list, @Nullable GoApplicationConfiguration.Kind kind, int index, boolean selected, boolean hasFocus) {
        if (kind != null) {
          String kindName = StringUtil.capitalize(kind.toString().toLowerCase(Locale.US));
          setText(kindName);
        }
      }
    };
  }

  private void installRunKindComboBox() {
    myRunKindComboBox.removeAllItems();
    myRunKindComboBox.setRenderer(getRunKindListCellRendererWrapper());
    for (GoApplicationConfiguration.Kind kind : GoApplicationConfiguration.Kind.values()) {
      myRunKindComboBox.addItem(kind);
    }
    myRunKindComboBox.addActionListener(e -> onRunKindChanged());
  }

  @Nonnull
  @Override
  protected JComponent createEditor() {
    return myCommonSettingsPanel;
  }
}
