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

package com.goide.runconfig.testing;

import com.goide.runconfig.GoConfigurationFactoryBase;
import com.goide.runconfig.before.GoBeforeRunTaskProvider;
import com.goide.runconfig.before.GoCommandBeforeRunTask;
import consulo.execution.BeforeRunTask;
import consulo.execution.configuration.ConfigurationTypeBase;
import consulo.util.dataholder.Key;

import jakarta.annotation.Nonnull;

public abstract class GoTestConfigurationFactoryBase extends GoConfigurationFactoryBase {
  public GoTestConfigurationFactoryBase(@Nonnull ConfigurationTypeBase type) {
    super(type);
  }

  @Override
  public void configureBeforeRunTaskDefaults(Key<? extends BeforeRunTask> providerID, BeforeRunTask task) {
    super.configureBeforeRunTaskDefaults(providerID, task);
    if (providerID == GoBeforeRunTaskProvider.ID && task instanceof GoCommandBeforeRunTask 
        && ((GoCommandBeforeRunTask)task).getCommand() == null) {
      task.setEnabled(true);
      ((GoCommandBeforeRunTask)task).setCommand("test -i");
    }
  }
}
