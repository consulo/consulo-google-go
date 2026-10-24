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

package com.goide.runconfig;

import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.go.localize.GoLocalize;
import consulo.localize.LocalizeValue;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.process.cmd.ParametersListUtil;
import consulo.project.Project;
import consulo.ui.TextBoxWithExpandAction;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.StringUtil;

public class GoToolArgumentsFragment<T extends GoRunConfigurationBase<?>> implements RunConfigurationFragment<T> {
    @Override
    public String getId() {
        return "goToolArguments";
    }

    @Override
    public LocalizeValue getName() {
        return GoLocalize.goRunConfigurationGoToolArgumentsTitle();
    }

    @Override
    public boolean isVisibleByDefault(T target) {
        return !target.getGoToolParams().isEmpty();
    }

    @Override
    public void applyHidden(T target) {
        target.setGoParams(null);
    }

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<T> createEditor(Project project, Disposable parent) {
        TextBoxWithExpandAction arguments = TextBoxWithExpandAction.create(
            PlatformIconGroup.actionsShow(),
            GoLocalize.goRunConfigurationGoToolArgumentsTitle().get(),
            ParametersListUtil.DEFAULT_LINE_PARSER,
            ParametersListUtil.DEFAULT_LINE_JOINER
        );

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(GoLocalize.goRunConfigurationGoToolArgumentsLabel(), arguments);
            }

            @RequiredUIAccess
            @Override
            public void reset(T target) {
                arguments.setValue(target.getGoToolParams(), false);
            }

            @RequiredUIAccess
            @Override
            public void apply(T target) {
                target.setGoParams(arguments.getValue());
            }
        };
    }
}
