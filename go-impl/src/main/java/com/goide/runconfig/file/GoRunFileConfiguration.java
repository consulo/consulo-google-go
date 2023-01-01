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

package com.goide.runconfig.file;

import com.goide.runconfig.GoModuleBasedConfiguration;
import com.goide.runconfig.GoRunConfigurationWithMain;
import com.goide.runconfig.ui.GoRunFileConfigurationEditorForm;
import consulo.application.ApplicationManager;
import consulo.execution.RuntimeConfigurationException;
import consulo.execution.configuration.ConfigurationType;
import consulo.execution.configuration.ModuleBasedConfiguration;
import consulo.execution.configuration.RunConfiguration;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.language.editor.scratch.ScratchUtil;
import consulo.module.Module;
import consulo.project.Project;
import consulo.util.io.FileUtil;
import consulo.util.io.PathUtil;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.UUID;

public class GoRunFileConfiguration extends GoRunConfigurationWithMain<GoRunFileRunningState> {
  public GoRunFileConfiguration(Project project, String name, @Nonnull ConfigurationType configurationType) {
    super(name, new GoModuleBasedConfiguration(project), configurationType.getConfigurationFactories()[0]);
  }

  @Nonnull
  @Override
  protected ModuleBasedConfiguration createInstance() {
    return new GoRunFileConfiguration(getProject(), getName(), GoRunFileConfigurationType.getInstance());
  }

  @Nonnull
  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new GoRunFileConfigurationEditorForm(getProject());
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    super.checkBaseConfiguration();
    super.checkFileConfiguration();
  }

  @Nonnull
  @Override
  protected GoRunFileRunningState newRunningState(@Nonnull ExecutionEnvironment env, @Nonnull Module module) {
    String path = getFilePath();
    if (!"go".equals(PathUtil.getFileExtension(path))) {
      VirtualFile f = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
      if (f != null && ScratchUtil.isScratch(f)) {
        String suffixWithoutExt = "." + UUID.randomUUID().toString().substring(0, 4);
        String suffix = suffixWithoutExt + ".go";
        String before = f.getName();
        String beforeWithoutExt = FileUtil.getNameWithoutExtension(before);
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          @Override
          public void run() {
            try {
              f.rename(this, before + suffix);
            }
            catch (IOException ignored) {
            }
          }
        });
        setFilePath(path + suffix);
        setName(getName().replace(beforeWithoutExt, beforeWithoutExt + suffixWithoutExt));
      }
    } 
    return new GoRunFileRunningState(env, module, this);
  }
}
