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

import com.goide.psi.GoFile;
import com.goide.psi.GoFunctionDeclaration;
import com.goide.psi.GoFunctionOrMethodDeclaration;
import com.goide.psi.GoMethodDeclaration;
import com.goide.runconfig.GoRunUtil;
import com.goide.sdk.GoSdkService;
import consulo.execution.action.ConfigurationContext;
import consulo.execution.action.RunConfigurationProducer;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.Project;
import consulo.util.io.FileUtil;
import consulo.util.lang.Comparing;
import consulo.util.lang.StringUtil;
import consulo.util.lang.ref.Ref;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class GoTestRunConfigurationProducerBase extends RunConfigurationProducer<GoTestRunConfiguration> {
  @Nonnull
  private final GoTestFramework myFramework;

  protected GoTestRunConfigurationProducerBase(@Nonnull GoTestFramework framework) {
    super(GoTestRunConfigurationType.getInstance());
    myFramework = framework;
  }

  @Override
  protected boolean setupConfigurationFromContext(@Nonnull GoTestRunConfiguration configuration,
                                                  ConfigurationContext context,
                                                  Ref sourceElement) {
    PsiElement contextElement = GoRunUtil.getContextElement(context);
    if (contextElement == null) {
      return false;
    }

    Module module = ModuleUtilCore.findModuleForPsiElement(contextElement);
    Project project = contextElement.getProject();
    if (module == null || !GoSdkService.getInstance(project).isGoModule(module)) return false;
    if (!myFramework.isAvailable(module)) return false;

    configuration.setModule(module);
    configuration.setTestFramework(myFramework);
    if (contextElement instanceof PsiDirectory) {
      configuration.setName(getPackageConfigurationName(((PsiDirectory)contextElement).getName()));
      configuration.setKind(GoTestRunConfiguration.Kind.DIRECTORY);
      String directoryPath = ((PsiDirectory)contextElement).getVirtualFile().getPath();
      configuration.setDirectoryPath(directoryPath);
      configuration.setWorkingDirectory(directoryPath);
      return true;
    }

    PsiFile file = contextElement.getContainingFile();
    if (myFramework.isAvailableOnFile(file)) {
      String importPath = ((GoFile)file).getImportPath(false);
      if (GoRunUtil.isPackageContext(contextElement) && StringUtil.isNotEmpty(importPath)) {
        configuration.setKind(GoTestRunConfiguration.Kind.PACKAGE);
        configuration.setPackage(importPath);
        configuration.setName(getPackageConfigurationName(importPath));
        return true;
      }
      else {
        GoFunctionOrMethodDeclaration function = findTestFunctionInContext(contextElement);
        if (function != null) {
          if (myFramework.isAvailableOnFunction(function)) {
            configuration.setName(getFunctionConfigurationName(function, file.getName()));
            configuration.setPattern("^" + function.getName() + "$");

            configuration.setKind(GoTestRunConfiguration.Kind.PACKAGE);
            configuration.setPackage(StringUtil.notNullize(((GoFile)file).getImportPath(false)));
            return true;
          }
        }
        else if (hasSupportedFunctions((GoFile)file)) {
          configuration.setName(getFileConfigurationName(file.getName()));
          configuration.setKind(GoTestRunConfiguration.Kind.FILE);
          configuration.setFilePath(file.getVirtualFile().getPath());
          return true;
        }
      }
    }
    return false;
  }

  private boolean hasSupportedFunctions(@Nonnull GoFile file) {
    for (GoFunctionDeclaration declaration : file.getFunctions()) {
      if (myFramework.isAvailableOnFunction(declaration)) {
        return true;
      }
    }
    for (GoMethodDeclaration declaration : file.getMethods()) {
      if (myFramework.isAvailableOnFunction(declaration)) {
        return true;
      }
    }
    return false;
  }

  @Nonnull
  protected String getFileConfigurationName(@Nonnull String fileName) {
    return fileName;
  }

  @Nonnull
  protected String getFunctionConfigurationName(@Nonnull GoFunctionOrMethodDeclaration function, @Nonnull String fileName) {
    return function.getName() + " in " + fileName;
  }

  @Nonnull
  protected String getPackageConfigurationName(@Nonnull String packageName) {
    return "All in '" + packageName + "'";
  }

  @Override
  public boolean isConfigurationFromContext(@Nonnull GoTestRunConfiguration configuration, ConfigurationContext context) {
    PsiElement contextElement = GoRunUtil.getContextElement(context);
    if (contextElement == null) return false;

    Module module = ModuleUtilCore.findModuleForPsiElement(contextElement);
    if (!Comparing.equal(module, configuration.getConfigurationModule().getModule())) return false;
    if (!Comparing.equal(myFramework, configuration.getTestFramework())) return false;

    PsiFile file = contextElement.getContainingFile();
    switch (configuration.getKind()) {
      case DIRECTORY:
        if (contextElement instanceof PsiDirectory) {
          String directoryPath = ((PsiDirectory)contextElement).getVirtualFile().getPath();
          return FileUtil.pathsEqual(configuration.getDirectoryPath(), directoryPath) &&
                 FileUtil.pathsEqual(configuration.getWorkingDirectory(), directoryPath);
        }
      case PACKAGE:
        if (!GoTestFinder.isTestFile(file)) return false;
        if (!Comparing.equal(((GoFile)file).getImportPath(false), configuration.getPackage())) return false;
        if (GoRunUtil.isPackageContext(contextElement) && configuration.getPattern().isEmpty()) return true;

        GoFunctionOrMethodDeclaration contextFunction = findTestFunctionInContext(contextElement);
        return contextFunction != null && myFramework.isAvailableOnFunction(contextFunction)
               ? configuration.getPattern().equals("^" + contextFunction.getName() + "$")
               : configuration.getPattern().isEmpty();
      case FILE:
        GoFunctionOrMethodDeclaration contextTestFunction = findTestFunctionInContext(contextElement);
        return contextTestFunction == null && GoTestFinder.isTestFile(file) && 
               FileUtil.pathsEqual(configuration.getFilePath(), file.getVirtualFile().getPath());
    }
    return false;
  }

  @Nullable
  private static GoFunctionOrMethodDeclaration findTestFunctionInContext(@Nonnull PsiElement contextElement) {
    GoFunctionOrMethodDeclaration function = PsiTreeUtil.getNonStrictParentOfType(contextElement, GoFunctionOrMethodDeclaration.class);
    return function != null && GoTestFunctionType.fromName(function.getName()) != null ? function : null;
  }
}