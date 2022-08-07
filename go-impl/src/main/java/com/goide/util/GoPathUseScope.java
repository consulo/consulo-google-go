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

import com.goide.psi.GoFile;
import com.goide.psi.GoImportSpec;
import com.goide.psi.GoNamedElement;
import com.goide.psi.impl.GoPsiImplUtil;
import com.goide.psi.impl.imports.GoImportReferenceSet;
import com.goide.sdk.GoPackageUtil;
import com.goide.sdk.GoSdkService;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.Project;
import consulo.util.lang.ObjectUtil;
import consulo.virtualFileSystem.VirtualFile;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

public class GoPathUseScope extends GlobalSearchScope {
  public static GlobalSearchScope create(@Nonnull PsiElement declarationContext, boolean filterByImportList) {
    if (declarationContext instanceof GoNamedElement && ((GoNamedElement)declarationContext).isBlank()) {
      return GlobalSearchScope.EMPTY_SCOPE;
    }
    PsiFile declarationPsiFile = declarationContext.getContainingFile();
    if (!(declarationPsiFile instanceof GoFile)) {
      return GlobalSearchScope.fileScope(declarationPsiFile);
    }
    if (GoPsiImplUtil.isBuiltinFile(declarationPsiFile)) {
      return GlobalSearchScope.allScope(declarationContext.getProject());
    }

    VirtualFile declarationFile = declarationPsiFile.getVirtualFile();
    if (declarationFile == null || declarationFile.getParent() == null) {
      return GlobalSearchScope.fileScope(declarationPsiFile);
    }

    return new GoPathUseScope(declarationPsiFile.getProject(), declarationFile, filterByImportList);
  }

  @Nonnull
  private final VirtualFile myDeclarationFile;
  private final boolean myFilterByImportList;

  private GoPathUseScope(@Nonnull Project project, @Nonnull VirtualFile declarationFile, boolean filterByImportList) {
    super(project);
    myDeclarationFile = declarationFile;
    myFilterByImportList = filterByImportList;
  }

  @Override
  public boolean contains(@Nonnull VirtualFile referenceFile) {
    VirtualFile referenceDirectory = referenceFile.isDirectory() ? referenceFile : referenceFile.getParent();
    if (referenceDirectory == null) {
      return false;
    }
    VirtualFile declarationDirectory = myDeclarationFile.getParent();
    if (referenceDirectory.equals(declarationDirectory)) {
      return true;
    }

    Project project = ObjectUtil.assertNotNull(getProject());
    PsiManager psiManager = PsiManager.getInstance(project);
    PsiFile referencePsiFile = psiManager.findFile(referenceFile);
    Module module = referencePsiFile != null ? ModuleUtilCore.findModuleForPsiElement(referencePsiFile) : null;

    GoPathScopeHelper scopeHelper = GoPathScopeHelper.fromReferenceFile(project, module, referenceFile);
    if (!scopeHelper.couldBeReferenced(myDeclarationFile, referenceFile)) {
      return false;
    }
    if (!myFilterByImportList) {
      return true;
    }

    if (!(referencePsiFile instanceof GoFile)) {
      // it's some injection or cross-reference, so we cannot check its imports
      return true;
    }
    PsiFile declarationPsiFile = psiManager.findFile(myDeclarationFile);
    if (declarationPsiFile instanceof GoFile) {
      String importPath = ((GoFile)declarationPsiFile).getImportPath(scopeHelper.isVendoringEnabled());
      Map<String, GoImportSpec> importedPackagesMap = ((GoFile)referencePsiFile).getImportedPackagesMap();
      if (importedPackagesMap.containsKey(importPath)) {
        return true;
      }
      if (hasRelativeImportOfTargetPackage(importedPackagesMap.keySet(), referenceDirectory, declarationDirectory)) {
        return true;
      }

      for (GoFile packageFile : GoPackageUtil.getAllPackageFiles(referencePsiFile.getContainingDirectory(), null)) {
        if (packageFile != referencePsiFile && referencePsiFile.getOriginalFile() != packageFile) {
          Map<String, GoImportSpec> packagesMap = packageFile.getImportedPackagesMap();
          if (packagesMap.containsKey(importPath)) {
            return true;
          }
          if (hasRelativeImportOfTargetPackage(packagesMap.keySet(), referenceDirectory, declarationDirectory)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private static boolean hasRelativeImportOfTargetPackage(@Nonnull Set<String> paths,
                                                          @Nonnull VirtualFile referenceDirectory,
                                                          @Nonnull VirtualFile declarationDirectory) {
    for (String pathString : paths) {
      if (GoImportReferenceSet.isRelativeImport(pathString)) {
        VirtualFile file = referenceDirectory.findFileByRelativePath(pathString);
        if (declarationDirectory.equals(file)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public int compare(@Nonnull VirtualFile file1, @Nonnull VirtualFile file2) {
    return 0;
  }

  @Override
  public boolean isSearchInModuleContent(@Nonnull Module aModule) {
    return GoSdkService.getInstance(ObjectUtil.assertNotNull(getProject())).isGoModule(aModule);
  }

  @Override
  public boolean isSearchInLibraries() {
    return false;
  }
}
