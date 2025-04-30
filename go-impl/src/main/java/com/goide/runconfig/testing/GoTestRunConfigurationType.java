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
package com.goide.runconfig.testing;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.execution.configuration.ConfigurationType;
import consulo.execution.configuration.ConfigurationTypeBase;
import consulo.execution.configuration.RunConfiguration;
import consulo.google.go.icon.GoogleGoIconGroup;
import consulo.google.go.localize.GoLocalize;
import consulo.project.Project;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class GoTestRunConfigurationType extends ConfigurationTypeBase {
    public GoTestRunConfigurationType() {
        super(
            "GoTestRunConfiguration",
            GoLocalize.goTestConfigurationName(),
            GoLocalize.goTestConfigurationDescription(),
            GoogleGoIconGroup.gotest()
        );
        addFactory(new GoTestConfigurationFactoryBase(this) {
            @Override
            @Nonnull
            public RunConfiguration createTemplateConfiguration(@Nonnull Project project) {
                return new GoTestRunConfiguration(project, "Go Test", getInstance());
            }
        });
    }

    @Nonnull
    public static GoTestRunConfigurationType getInstance() {
        return Application.get().getExtensionPoint(ConfigurationType.class)
            .findExtensionOrFail(GoTestRunConfigurationType.class);
    }
}
