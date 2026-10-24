/*
 * Copyright 2013-2026 consulo.io
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

package com.goide.runconfig.application;

import com.goide.runconfig.GoRunUtil;
import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.go.localize.GoLocalize;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.StringUtil;

public class GoOutputDirectoryFragment implements RunConfigurationFragment<GoApplicationConfiguration> {
    @Override
    public String getId() {
        return "goOutputDirectory";
    }

    @Override
    public LocalizeValue getName() {
        return GoLocalize.runConfigurationFragmentOutputDirectory();
    }

    @Override
    public boolean isVisibleByDefault(GoApplicationConfiguration target) {
        return !target.getOutputDirectory().isEmpty();
    }

    @Override
    public void applyHidden(GoApplicationConfiguration target) {
        target.setOutputDirectory(null);
    }

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<GoApplicationConfiguration> createEditor(Project project, Disposable parent) {
        FileChooserTextBoxBuilder.Controller directory = FileChooserTextBoxBuilder.create(project)
            .uiDisposable(parent)
            .fileChooserDescriptor(GoRunUtil.createFileChooserDescriptor(project, true, true, null))
            .build();

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(GoLocalize.goRunConfigurationOutputDirectoryLabel(), directory.getComponent());
            }

            @RequiredUIAccess
            @Override
            public void reset(GoApplicationConfiguration target) {
                directory.setValue(target.getOutputDirectory(), false);
            }

            @RequiredUIAccess
            @Override
            public void apply(GoApplicationConfiguration target) {
                target.setOutputDirectory(StringUtil.notNullize(directory.getValue()).trim());
            }
        };
    }
}
