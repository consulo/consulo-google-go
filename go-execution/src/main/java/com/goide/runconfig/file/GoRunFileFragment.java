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

package com.goide.runconfig.file;

import com.goide.runconfig.GoRunUtil;
import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.execution.configuration.fragment.StandardRunConfigurationFragmentOrder;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.go.localize.GoLocalize;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.StringUtil;

public class GoRunFileFragment implements RunConfigurationFragment<GoRunFileConfiguration> {
    @Override
    public String getId() {
        return "goRunFile";
    }

    @Override
    public LocalizeValue getName() {
        return GoLocalize.runConfigurationFragmentFile();
    }

    @Override
    public int getOrder() {
        return StandardRunConfigurationFragmentOrder.FIRST;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<GoRunFileConfiguration> createEditor(Project project, Disposable parent) {
        FileChooserTextBoxBuilder.Controller file = FileChooserTextBoxBuilder.create(project)
            .uiDisposable(parent)
            .fileChooserDescriptor(GoRunUtil.createGoWithMainFileChooserDescriptor(project))
            .build();

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(GoLocalize.goRunConfigurationFileLabel(), file.getComponent());
            }

            @RequiredUIAccess
            @Override
            public void reset(GoRunFileConfiguration target) {
                file.setValue(target.getFilePath(), false);
            }

            @RequiredUIAccess
            @Override
            public void apply(GoRunFileConfiguration target) {
                target.setFilePath(StringUtil.notNullize(file.getValue()).trim());
            }
        };
    }
}
