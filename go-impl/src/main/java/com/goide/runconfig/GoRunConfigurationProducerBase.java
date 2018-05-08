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

package com.goide.runconfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.goide.psi.GoFile;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public abstract class GoRunConfigurationProducerBase<T extends GoRunConfigurationWithMain> extends RunConfigurationProducer<T> implements Cloneable {
  protected GoRunConfigurationProducerBase(@Nonnull ConfigurationType configurationType) {
    super(configurationType);
  }

  @Override
  protected boolean setupConfigurationFromContext(@Nonnull T configuration, @Nonnull ConfigurationContext context, Ref<PsiElement> sourceElement) {
    PsiFile file = getFileFromContext(context);
    if (GoRunUtil.isMainGoFile(file)) {
      configuration.setName(getConfigurationName(file));
      configuration.setFilePath(file.getVirtualFile().getPath());
      Module module = context.getModule();
      if (module != null) {
        configuration.setModule(module);
      }
      return true;
    }
    return false;
  }

  @Nonnull
  protected abstract String getConfigurationName(@Nonnull PsiFile file);

  @Override
  public boolean isConfigurationFromContext(@Nonnull T configuration, ConfigurationContext context) {
    GoFile file = getFileFromContext(context);
    return file != null && FileUtil.pathsEqual(configuration.getFilePath(), file.getVirtualFile().getPath());
  }

  @Nullable
  private static GoFile getFileFromContext(@Nullable ConfigurationContext context) {
    PsiElement contextElement = GoRunUtil.getContextElement(context);
    PsiFile psiFile = contextElement != null ? contextElement.getContainingFile() : null;
    return psiFile instanceof GoFile ? (GoFile)psiFile : null;
  }
}
