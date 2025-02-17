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

import com.goide.codeInsight.imports.GoGetPackageFix;
import com.goide.completion.GoCompletionUtil;
import com.goide.quickfix.GoDeleteImportQuickFix;
import com.goide.sdk.GoPackageUtil;
import consulo.document.util.TextRange;
import consulo.language.editor.completion.CompletionUtilCore;
import consulo.language.editor.impl.intention.CreateFileFix;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.psi.*;
import consulo.language.psi.path.FileReference;
import consulo.language.psi.path.FileReferenceSet;
import consulo.language.util.IncorrectOperationException;
import consulo.util.collection.ArrayUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.*;

public class GoImportReference extends FileReference {
  public GoImportReference(@Nonnull FileReferenceSet fileReferenceSet, TextRange range, int index, String text) {
    super(fileReferenceSet, range, index, text);
  }

  @Override
  public Object createLookupItem(PsiElement candidate) {
    if (candidate instanceof PsiDirectory) {
      return GoCompletionUtil.createDirectoryLookupElement((PsiDirectory)candidate);
    }
    return super.createLookupItem(candidate);
  }

  @Nonnull
  @Override
  protected ResolveResult[] innerResolve(boolean caseSensitive, @Nonnull PsiFile file) {
    if (isFirst()) {
      if (".".equals(getCanonicalText())) {
        PsiDirectory directory = getDirectory();
        return directory != null ? new PsiElementResolveResult[]{new PsiElementResolveResult(directory)} : ResolveResult.EMPTY_ARRAY;
      }
      else if ("..".equals(getCanonicalText())) {
        PsiDirectory directory = getDirectory();
        PsiDirectory grandParent = directory != null ? directory.getParentDirectory() : null;
        return grandParent != null ? new PsiElementResolveResult[]{new PsiElementResolveResult(grandParent)} : ResolveResult.EMPTY_ARRAY;
      }
    }

    String referenceText = getText();
    Set<ResolveResult> result = new LinkedHashSet<>();
    Set<ResolveResult> innerResult = new LinkedHashSet<>();
    for (PsiFileSystemItem context : getContexts()) {
      innerResolveInContext(referenceText, context, innerResult, caseSensitive);
      for (ResolveResult resolveResult : innerResult) {
        PsiElement element = resolveResult.getElement();
        if (element instanceof PsiDirectory) {
          if (isLast()) {
            return new ResolveResult[]{resolveResult};
          }
          result.add(resolveResult);
        }
      }
      innerResult.clear();
    }
    return result.isEmpty() ? ResolveResult.EMPTY_ARRAY : result.toArray(new ResolveResult[result.size()]);
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    if (super.isReferenceTo(element)) {
      return true;
    }

    if (element instanceof PsiDirectoryContainer) {
      for (PsiDirectory directory : ((PsiDirectoryContainer)element).getDirectories()) {
        if (super.isReferenceTo(directory)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public PsiElement bindToElement(@Nonnull PsiElement element, boolean absolute) throws IncorrectOperationException {
    if (!absolute) {
      FileReference firstReference = ArrayUtil.getFirstElement(getFileReferenceSet().getAllReferences());
      if (firstReference != null) {
        Collection<PsiFileSystemItem> contexts = getFileReferenceSet().getDefaultContexts();
        for (ResolveResult resolveResult : firstReference.multiResolve(false)) {
          PsiElement resolveResultElement = resolveResult.getElement();
          if (resolveResultElement instanceof PsiFileSystemItem) {
            PsiFileSystemItem parentDirectory = ((PsiFileSystemItem)resolveResultElement).getParent();
            if (parentDirectory != null && contexts.contains(parentDirectory)) {
              return getElement();
            }
          }
        }
      }
    }
    return super.bindToElement(element, absolute);
  }

  @Nonnull
  public LocalQuickFix[] getQuickFixes() {
    if (GoPackageUtil.isBuiltinPackage(resolve())) {
      return new LocalQuickFix[]{new GoDeleteImportQuickFix()};
    }

    List<LocalQuickFix> result = new ArrayList<>();
    FileReferenceSet fileReferenceSet = getFileReferenceSet();
    if (fileReferenceSet instanceof GoImportReferenceSet && !((GoImportReferenceSet)fileReferenceSet).isRelativeImport()
        && !fileReferenceSet.isAbsolutePathReference()) {
      result.add(new GoGetPackageFix(fileReferenceSet.getPathString()));
    }

    String fileNameToCreate = getFileNameToCreate();
    for (PsiFileSystemItem context : getContexts()) {
      if (context instanceof PsiDirectory) {
        try {
          ((PsiDirectory)context).checkCreateSubdirectory(fileNameToCreate);
          String targetPath = context.getVirtualFile().getPath();
          result.add(new CreateFileFix(true, fileNameToCreate, (PsiDirectory)context) {
            @Nonnull
            @Override
            public String getText() {
              return "Create Directory " + fileNameToCreate + " at " + targetPath;
            }
          });
        }
        catch (IncorrectOperationException ignore) {
        }
      }
    }
    return result.toArray(new LocalQuickFix[result.size()]);
  }

  private boolean isFirst() {
    return getIndex() <= 0;
  }

  @Nullable
  private PsiDirectory getDirectory() {
    PsiElement originalElement = CompletionUtilCore.getOriginalElement(getElement());
    PsiFile file = originalElement != null ? originalElement.getContainingFile() : getElement().getContainingFile();
    return file.getParent();
  }
}
