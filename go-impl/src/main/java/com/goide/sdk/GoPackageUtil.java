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

package com.goide.sdk;

import com.goide.GoConstants;
import com.goide.project.GoBuildTargetSettings;
import com.goide.psi.GoFile;
import com.goide.psi.impl.GoPsiImplUtil;
import consulo.application.util.CachedValue;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.google.go.module.extension.GoModuleExtension;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileSystemItem;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.content.ProjectRootManager;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.dataholder.Key;
import consulo.util.io.FileUtil;
import consulo.util.lang.Comparing;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public class GoPackageUtil {
  private static final Key<CachedValue<Collection<String>>> PACKAGES_CACHE = Key.create("packages_cache");
  private static final Key<CachedValue<Collection<String>>> PACKAGES_TEST_TRIMMED_CACHE = Key.create("packages_test_trimmed_cache");

  private GoPackageUtil() {}

  @Contract("null -> false")
  public static boolean isBuiltinPackage(@Nullable PsiFileSystemItem directory) {
    return directory instanceof PsiDirectory
           && GoConstants.BUILTIN_PACKAGE_NAME.equals(directory.getName())
           && GoConstants.BUILTIN_PACKAGE_NAME.equals(GoSdkUtil.getImportPath((PsiDirectory)directory, false));
  }

  @Nonnull
  public static List<GoFile> getAllPackageFiles(@Nonnull GoFile file) {
    String packageName = file.getPackageName();
    PsiDirectory parent = file.getParent();
    if (parent == null || StringUtil.isEmpty(packageName)) return ContainerUtil.list(file);
    return getAllPackageFiles(parent, packageName);
  }
  
  @Nonnull
  public static List<GoFile> getAllPackageFiles(@Nullable PsiDirectory directory, @Nullable String packageName) {
    if (directory == null) {
      return Collections.emptyList();
    }
    PsiElement[] children = directory.getChildren();
    List<GoFile> files = ContainerUtil.newArrayListWithCapacity(children.length);
    for (PsiElement element : children) {
      if (element instanceof GoFile && (packageName == null || Comparing.equal(((GoFile)element).getPackageName(), packageName))) {
        files.add((GoFile)element);
      }
    }
    return files;
  }

  @Nonnull
  public static GlobalSearchScope packageScope(@Nonnull GoFile file) {
    List<GoFile> files = getAllPackageFiles(file);
    return GlobalSearchScope.filesWithLibrariesScope(file.getProject(), ContainerUtil.map(files, PsiFile::getVirtualFile));
  }
  
  @Nonnull
  public static GlobalSearchScope packageScope(@Nonnull PsiDirectory psiDirectory, @Nullable String packageName) {
    List<GoFile> files = getAllPackageFiles(psiDirectory, packageName);
    return GlobalSearchScope.filesWithLibrariesScope(psiDirectory.getProject(), ContainerUtil.map(files, PsiFile::getVirtualFile));
  }

  @Nonnull
  public static Collection<String> getAllPackagesInDirectory(@Nullable PsiDirectory dir,
                                                             @Nullable Module contextModule,
                                                             boolean trimTestSuffices) {
    if (dir == null) return Collections.emptyList();
    if (contextModule != null) {
      return getAllPackagesInDirectoryInner(dir, contextModule, trimTestSuffices);
    }
    Key<CachedValue<Collection<String>>> key = trimTestSuffices ? PACKAGES_TEST_TRIMMED_CACHE : PACKAGES_CACHE;
    return CachedValuesManager.getManager(dir.getProject()).getCachedValue(dir, key, () -> {
      Module module = ModuleUtilCore.findModuleForPsiElement(dir);
      GoModuleExtension moduleExtension = module != null ? ModuleUtilCore.getExtension(module, GoModuleExtension.class) : null;
      GoBuildTargetSettings buildTargetSettings = moduleExtension != null ? moduleExtension.getBuildTargetSettings() : null;
      // todo[zolotov]: implement package modification tracker
      return buildTargetSettings != null
             ? CachedValueProvider.Result.create(getAllPackagesInDirectoryInner(dir, module, trimTestSuffices), dir, ProjectRootManager.getInstance(dir.getProject()))
             : CachedValueProvider.Result.create(getAllPackagesInDirectoryInner(dir, null, trimTestSuffices), dir); 
    }, false);
  }

  @Nonnull
  private static Collection<String> getAllPackagesInDirectoryInner(@Nonnull PsiDirectory dir,
                                                                   @Nullable Module contextModule,
                                                                   boolean trimTestSuffices) {
    Collection<String> set = new LinkedHashSet<>();
    for (PsiFile file : dir.getFiles()) {
      if (file instanceof GoFile && GoPsiImplUtil.allowed(file, null, contextModule)) {
        String name = trimTestSuffices ? ((GoFile)file).getCanonicalPackageName() : ((GoFile)file).getPackageName();
        if (StringUtil.isNotEmpty(name)) {
          set.add(name);
        }
      }
    }
    return set;
  }

  @Nullable
  public static VirtualFile findByImportPath(@Nonnull String importPath, @Nonnull Project project, @Nullable Module module) {
    if (importPath.isEmpty()) {
      return null;
    }
    importPath = FileUtil.toSystemIndependentName(importPath);
    for (VirtualFile root : GoSdkUtil.getSourcesPathsToLookup(project, module)) {
      VirtualFile file = root.findFileByRelativePath(importPath);
      if (file != null) {
        return file;
      }
    }
    return null;
  }
}
