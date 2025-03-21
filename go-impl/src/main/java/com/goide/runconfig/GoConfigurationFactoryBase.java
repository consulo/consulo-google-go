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

package com.goide.runconfig;

import com.goide.compiler.GoCompiler;
import consulo.compiler.execution.CompileStepBeforeRun;
import consulo.execution.BeforeRunTask;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.ConfigurationType;
import consulo.google.go.module.extension.GoModuleExtension;
import consulo.module.extension.ModuleExtensionHelper;
import consulo.project.Project;
import consulo.util.dataholder.Key;
import jakarta.annotation.Nonnull;

public abstract class GoConfigurationFactoryBase extends ConfigurationFactory {
    protected GoConfigurationFactoryBase(ConfigurationType type) {
        super(type);
    }

    @Override
    public void configureBeforeRunTaskDefaults(Key<? extends BeforeRunTask> providerID, BeforeRunTask task) {
        super.configureBeforeRunTaskDefaults(providerID, task);

        if (!GoCompiler.ENABLE_COMPILER) {
            if (providerID == CompileStepBeforeRun.ID) {
                task.setEnabled(false);
            }
        }
    }

    @Override
    public boolean isApplicable(@Nonnull Project project) {
        return ModuleExtensionHelper.getInstance(project).hasModuleExtension(GoModuleExtension.class);
    }
}
