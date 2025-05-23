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

import com.goide.psi.GoFile;
import com.goide.runconfig.GoRunConfigurationProducerBase;
import com.goide.runconfig.GoRunUtil;
import com.goide.runconfig.testing.GoTestFinder;
import com.goide.sdk.GoSdkUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.execution.action.ConfigurationContext;
import consulo.google.go.module.extension.GoModuleExtension;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.util.lang.Comparing;
import consulo.util.lang.StringUtil;
import consulo.util.lang.ref.SimpleReference;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class GoApplicationRunConfigurationProducer extends GoRunConfigurationProducerBase<GoApplicationConfiguration> implements Cloneable {
  public GoApplicationRunConfigurationProducer() {
    super(GoApplicationRunConfigurationType.getInstance());
  }

  @Override
  protected boolean setupConfigurationFromContext(@Nonnull GoApplicationConfiguration configuration,
                                                  @Nonnull ConfigurationContext context,
                                                  SimpleReference<PsiElement> sourceElement) {
    PsiElement contextElement = GoRunUtil.getContextElement(context);
    if (contextElement != null && GoTestFinder.isTestFile(contextElement.getContainingFile())) {
      return false;
    }

    Module module = context.getModule();
    if (module == null || ModuleUtilCore.getExtension(module, GoModuleExtension.class) == null) {
      return false;
    }
    
    String importPath = getImportPathFromContext(contextElement);
    if (StringUtil.isNotEmpty(importPath)) {
      configuration.setModule(context.getModule());
      configuration.setKind(GoApplicationConfiguration.Kind.PACKAGE);
      configuration.setPackage(importPath);
      configuration.setName("Build " + importPath + " and run");
      return true;
    }
    if (super.setupConfigurationFromContext(configuration, context, sourceElement)) {
      configuration.setKind(GoApplicationConfiguration.Kind.FILE);
      return true;
    }
    return false;
  }

  @Nullable
  private static String getImportPathFromContext(@Nullable PsiElement contextElement) {
    if (GoRunUtil.isPackageContext(contextElement)) {
      PsiFile file = contextElement.getContainingFile();
      if (file instanceof GoFile) {
        return ((GoFile)file).getImportPath(false);
      }
    }
    else if (contextElement instanceof PsiDirectory) {
      return GoSdkUtil.getImportPath((PsiDirectory)contextElement, false);
    }
    return null;
  }

  @Override
  public boolean isConfigurationFromContext(@Nonnull GoApplicationConfiguration configuration, ConfigurationContext context) {
    PsiElement contextElement = GoRunUtil.getContextElement(context);
    if (contextElement == null) return false;

    Module module = ModuleUtilCore.findModuleForPsiElement(contextElement);
    if (!Comparing.equal(module, configuration.getConfigurationModule().getModule())) return false;

    if (configuration.getKind() == GoApplicationConfiguration.Kind.PACKAGE) {
      return Comparing.equal(getImportPathFromContext(contextElement), configuration.getPackage());
    }

    return super.isConfigurationFromContext(configuration, context);
  }

  @Nonnull
  @Override
  protected String getConfigurationName(@Nonnull PsiFile file) {
    return "Build " + file.getName() + " and run";
  }
}
