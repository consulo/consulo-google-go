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

package com.goide.util;

import com.goide.GoConstants;
import com.goide.project.GoExcludedPathsSettings;
import com.goide.psi.*;
import com.goide.psi.impl.GoPsiImplUtil;
import com.goide.runconfig.testing.GoTestFinder;
import com.goide.sdk.GoPackageUtil;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.SystemInfo;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.DelegatingGlobalSearchScope;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import consulo.util.lang.ThreeState;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class GoUtil {
  private GoUtil() {}

  public static boolean matchedForModuleBuildTarget(@Nonnull PsiFile file, @Nullable Module module) {
    GoTargetSystem target = module == null ? null : GoTargetSystem.forModule(module);
    return target == null || new GoBuildMatcher(target).matchFile(file);
  }

  public static boolean isExcludedFile(@Nonnull GoFile file) {
    return LanguageCachedValueUtil.getCachedValue(file, () -> {
      String importPath = file.getImportPath(false);
      GoExcludedPathsSettings excludedSettings = GoExcludedPathsSettings.getInstance(file.getProject());
      return CachedValueProvider.Result.create(importPath != null && excludedSettings.isExcluded(importPath), file, excludedSettings);
    });
  }

  @Nonnull
  public static String systemOS() {
    // TODO android? dragonfly nacl? netbsd openbsd plan9
    if (SystemInfo.isMac) {
      return "darwin";
    }
    if (SystemInfo.isFreeBSD) {
      return "freebsd";
    }
    if (SystemInfo.isLinux) {
      return GoConstants.LINUX_OS;
    }
    if (SystemInfo.isWindows) {
      return "windows";
    }
    return "unknown";
  }

  @Nonnull
  public static String systemArch() {
    if (SystemInfo.is64Bit) {
      return GoConstants.AMD64;
    }
    if (SystemInfo.isWindows) {
      String arch = System.getenv("PROCESSOR_ARCHITECTURE");
      String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
      return arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64") ? GoConstants.AMD64 : "386";
    }
    if (SystemInfo.is32Bit) {
      return "386";
    }
    return "unknown";
  }

  @Nonnull
  public static ThreeState systemCgo(@Nonnull String os, @Nonnull String arch) {
    return GoConstants.KNOWN_CGO.contains(os + "/" + arch) ? ThreeState.YES : ThreeState.NO;
  }

  public static boolean fileToIgnore(@Nonnull String fileName) {
    return StringUtil.startsWithChar(fileName, '_') || StringUtil.startsWithChar(fileName, '.');
  }
  
  public static GlobalSearchScope goPathUseScope(@Nonnull PsiElement context, boolean filterByImportList) {
    return GoPathUseScope.create(context, filterByImportList);
  }

  public static GlobalSearchScope goPathResolveScope(@Nonnull PsiElement context) {
    // it's important to ask module on file, otherwise module won't be found for elements in libraries files [zolotov]
    Module module = ModuleUtilCore.findModuleForPsiElement(context.getContainingFile());
    return GoPathResolveScope.create(context.getProject(), module, context);
  }

  public static GlobalSearchScope goPathResolveScope(@Nonnull Module module, @Nullable PsiElement context) {
    return GoPathResolveScope.create(module.getProject(), module, context);
  }

  @Nonnull
  public static GlobalSearchScope moduleScope(@Nonnull Module module) {
    return GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module).uniteWith(GlobalSearchScope.moduleContentWithDependenciesScope(module));
  }

  @Nonnull
  public static GlobalSearchScope moduleScopeWithoutLibraries(@Nonnull Project project, @Nullable Module module) {
    return module != null ? GlobalSearchScope.moduleWithDependenciesScope(module).uniteWith(GlobalSearchScope.moduleContentWithDependenciesScope(module))
                          : GlobalSearchScope.projectScope(project);
  }


  /**
   * isReferenceTo optimization. Before complex checking via resolve we can say for sure that element
   * can't be a reference to given declaration in following cases:<br/>
   * – Blank definitions can't be used as value, so this method return false for all named elements with '_' name<br/>
   * – GoLabelRef can't be resolved to anything but GoLabelDefinition<br/>
   * – GoTypeReferenceExpression (not from receiver type) can't be resolved to anything but GoTypeSpec or GoImportSpec<br/>
   * – Definition is private and reference in different package<br/>
   */
  public static boolean couldBeReferenceTo(@Nonnull PsiElement definition, @Nonnull PsiElement reference) {
    if (definition instanceof PsiDirectory && reference instanceof GoReferenceExpressionBase) return true;
    if (reference instanceof GoLabelRef && !(definition instanceof GoLabelDefinition)) return false;
    if (reference instanceof GoTypeReferenceExpression &&
        !(definition instanceof GoTypeSpec || definition instanceof GoImportSpec)) {
      return false;
    }

    PsiFile definitionFile = definition.getContainingFile();
    PsiFile referenceFile = reference.getContainingFile();
    // todo: zolotov, are you sure? cross refs, for instance?
    if (!(definitionFile instanceof GoFile) || !(referenceFile instanceof GoFile)) return false;

    boolean inSameFile = definitionFile.isEquivalentTo(referenceFile);
    if (inSameFile) return true;

    if (inSamePackage(referenceFile, definitionFile)) return true;
    return !(reference instanceof GoNamedElement && !((GoNamedElement)reference).isPublic());
  }

  public static boolean inSamePackage(@Nonnull PsiFile firstFile, @Nonnull PsiFile secondFile) {
    PsiDirectory containingDirectory = firstFile.getContainingDirectory();
    if (containingDirectory == null || !containingDirectory.equals(secondFile.getContainingDirectory())) {
      return false;
    }
    if (firstFile instanceof GoFile && secondFile instanceof GoFile) {
      String referencePackage = ((GoFile)firstFile).getPackageName();
      String definitionPackage = ((GoFile)secondFile).getPackageName();
      return referencePackage != null && referencePackage.equals(definitionPackage);
    }
    return true;
  }

  @Nonnull
  public static String suggestPackageForDirectory(@Nullable PsiDirectory directory) {
    String packageName = GoPsiImplUtil.getLocalPackageName(directory != null ? directory.getName() : "");
    for (String p : GoPackageUtil.getAllPackagesInDirectory(directory, null, true)) {
      if (!GoConstants.MAIN.equals(p)) {
        return p;
      }
    }
    return packageName;
  }

  public static class ExceptTestsScope extends DelegatingGlobalSearchScope {
    public ExceptTestsScope(@Nonnull GlobalSearchScope baseScope) {
      super(baseScope);
    }

    @Override
    public boolean contains(@Nonnull VirtualFile file) {
      return !GoTestFinder.isTestFile(file) && super.contains(file);
    }
  }
  
  public static class TestsScope extends DelegatingGlobalSearchScope {
    public TestsScope(@Nonnull GlobalSearchScope baseScope) {
      super(baseScope);
    }

    @Override
    public boolean contains(@Nonnull VirtualFile file) {
      return GoTestFinder.isTestFile(file) && super.contains(file);
    }
  }

  public static class ExceptChildOfDirectory extends DelegatingGlobalSearchScope {
    @Nonnull
	private final VirtualFile myParent;
    @Nullable private final String myAllowedPackageInExcludedDirectory;

    public ExceptChildOfDirectory(@Nonnull VirtualFile parent,
                                  @Nonnull GlobalSearchScope baseScope,
                                  @Nullable String allowedPackageInExcludedDirectory) {
      super(baseScope);
      myParent = parent;
      myAllowedPackageInExcludedDirectory = allowedPackageInExcludedDirectory;
    }

    @Override
    public boolean contains(@Nonnull VirtualFile file) {
      if (myParent.equals(file.getParent())) {
        if (myAllowedPackageInExcludedDirectory == null) {
          return false;
        }
        Project project = getProject();
        PsiFile psiFile = project != null ? PsiManager.getInstance(project).findFile(file) : null;
        if (!(psiFile instanceof GoFile)) {
          return false;
        }
        if (!myAllowedPackageInExcludedDirectory.equals(((GoFile)psiFile).getPackageName())) {
          return false;
        }
      }
      return super.contains(file);
    }
  }
}