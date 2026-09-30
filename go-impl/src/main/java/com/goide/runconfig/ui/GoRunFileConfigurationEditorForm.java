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

import com.goide.runconfig.GoRunUtil;
import com.goide.runconfig.file.GoRunFileConfiguration;
import consulo.configurable.ConfigurationException;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.google.go.localize.GoLocalize;
import consulo.project.Project;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.Nullable;

public class GoRunFileConfigurationEditorForm extends SettingsEditor<GoRunFileConfiguration> {
  private final Project myProject;
  private @Nullable Panel myPanel;

  public GoRunFileConfigurationEditorForm(Project project) {
    myProject = project;
  }

  @RequiredUIAccess
  @Override
  protected Component createUIComponent() {
    Panel panel = new Panel();
    myPanel = panel;
    return panel.build();
  }

  @RequiredUIAccess
  @Override
  protected void resetEditorFrom(GoRunFileConfiguration configuration) {
    Panel panel = myPanel;
    if (panel != null) {
      panel.reset(configuration);
    }
  }

  @RequiredUIAccess
  @Override
  protected void applyEditorTo(GoRunFileConfiguration configuration) throws ConfigurationException {
    Panel panel = myPanel;
    if (panel != null) {
      panel.apply(configuration);
    }
  }

  private class Panel extends GoCommonSettingsPanel {
    private final FileChooserTextBoxBuilder.Controller myFileField;

    @RequiredUIAccess
    private Panel() {
      super(GoRunFileConfigurationEditorForm.this.myProject);

      myFileField = FileChooserTextBoxBuilder.create(myProject)
        .fileChooserDescriptor(GoRunUtil.createGoWithMainFileChooserDescriptor(myProject))
        .build();
    }

    @RequiredUIAccess
    @Override
    protected void addBefore(FormBuilder builder) {
      builder.addLabeled(GoLocalize.goRunConfigurationFileLabel(), myFileField.getComponent());
    }

    @RequiredUIAccess
    private void reset(GoRunFileConfiguration configuration) {
      myFileField.setValue(StringUtil.notNullize(configuration.getFilePath()));
      resetEditorFrom(configuration);
    }

    @RequiredUIAccess
    private void apply(GoRunFileConfiguration configuration) {
      configuration.setFilePath(StringUtil.notNullize(myFileField.getValue()));
      applyEditorTo(configuration);
    }
  }
}
