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

package com.goide.runconfig.application;

import com.goide.GoConstants;
import com.goide.runconfig.GoConfigurationFactoryBase;
import consulo.annotation.component.ExtensionImpl;
import consulo.execution.configuration.ConfigurationTypeBase;
import consulo.execution.configuration.RunConfiguration;
import consulo.google.go.icon.GoogleGoIconGroup;
import consulo.google.go.localize.GoLocalize;
import consulo.project.Project;

import javax.annotation.Nonnull;

@ExtensionImpl
public class GoApplicationRunConfigurationType extends ConfigurationTypeBase {
  public GoApplicationRunConfigurationType() {
    super("GoApplicationRunConfiguration", GoLocalize.goApplicationConfigurationName(), GoLocalize.goApplicationConfigurationDescription(),
          GoogleGoIconGroup.goapp());
    addFactory(new GoConfigurationFactoryBase(this) {
      @Override
      @Nonnull
      public RunConfiguration createTemplateConfiguration(@Nonnull Project project) {
        return new GoApplicationConfiguration(project, GoConstants.GO, getInstance());
      }
    });
  }

  @Nonnull
  public static GoApplicationRunConfigurationType getInstance() {
    return EP_NAME.findExtensionOrFail(GoApplicationRunConfigurationType.class);
  }
}
