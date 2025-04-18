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

package com.goide.psi.impl.imports;

import com.goide.project.GoVendoringUtil;
import com.goide.psi.GoImportString;
import com.goide.sdk.GoSdkUtil;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileSystemItem;
import consulo.language.psi.PsiManager;
import consulo.language.psi.path.FileReference;
import consulo.language.psi.path.FileReferenceSet;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.function.Predicates;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.function.Predicate;

public class GoImportReferenceSet extends FileReferenceSet {
  public GoImportReferenceSet(@Nonnull GoImportString importString) {
    super(importString.getPath(), importString, importString.getPathTextRange().getStartOffset(), null, true);
  }

  @Nonnull
  @Override
  public Collection<PsiFileSystemItem> computeDefaultContexts() {
    PsiFile file = getContainingFile();
    if (file == null || !file.isValid() || isAbsolutePathReference()) {
      return Collections.emptyList();
    }

    PsiManager psiManager = file.getManager();
    Module module = ModuleUtilCore.findModuleForPsiElement(file);
    Project project = file.getProject();
    LinkedHashSet<VirtualFile> sourceRoots = GoVendoringUtil.isVendoringEnabled(module)
                                             ? GoSdkUtil.getVendoringAwareSourcesPathsToLookup(project, module, file.getVirtualFile())
                                             : GoSdkUtil.getSourcesPathsToLookup(project, module);
    return ContainerUtil.mapNotNull(sourceRoots, psiManager::findDirectory);
  }

  @Override
  public Predicate<PsiFileSystemItem> getReferenceCompletionFilter() {
    if (!isRelativeImport()) {
      return Predicates.alwaysFalse();
    }
    return super.getReferenceCompletionFilter();
  }

  @Nullable
  @Override
  public PsiFileSystemItem resolve() {
    return isAbsolutePathReference() ? null : super.resolve();
  }

  @Override
  public boolean absoluteUrlNeedsStartSlash() {
    return false;
  }

  @Override
  public boolean isEndingSlashNotAllowed() {
    return !isRelativeImport();
  }

  @Nonnull
  @Override
  public FileReference createFileReference(TextRange range, int index, String text) {
    return new GoImportReference(this, range, index, text);
  }

  public boolean isRelativeImport() {
    return isRelativeImport(getPathString());
  }

  public static boolean isRelativeImport(@Nonnull String pathString) {
    return pathString.startsWith("./") || pathString.startsWith("../");
  }

  @Override
  public boolean couldBeConvertedTo(boolean relative) {
    return false;
  }
}
