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

import com.goide.GoIcons;
import com.goide.util.SingleCharInsertHandler;
import consulo.annotation.access.RequiredReadAction;
import consulo.document.util.TextRange;
import consulo.go.modules.GoImportPathMap;
import consulo.language.editor.completion.CompletionUtilCore;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.*;
import consulo.language.psi.path.FileReference;
import consulo.language.psi.path.FileReferenceSet;
import consulo.language.util.IncorrectOperationException;
import consulo.language.util.ModuleUtilCore;
import consulo.util.collection.ArrayUtil;
import consulo.virtualFileSystem.VirtualFile;

import org.jspecify.annotations.Nullable;
import java.util.*;

public class GoImportReference extends FileReference {
  private static final SingleCharInsertHandler DIR_INSERT_HANDLER = new SingleCharInsertHandler('/');

  public GoImportReference(FileReferenceSet fileReferenceSet, TextRange range, int index, String text) {
    super(fileReferenceSet, range, index, text);
  }

  @Override
  public Object createLookupItem(PsiElement candidate) {
    if (candidate instanceof PsiDirectory) {
      return createDirectoryLookupElement((PsiDirectory)candidate);
    }
    return super.createLookupItem(candidate);
  }

  @Override
  protected ResolveResult[] innerResolve(boolean caseSensitive, PsiFile file) {
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

    ResolveResult[] moduleResults = resolveInGoModules(file);
    if (moduleResults.length > 0) {
      return moduleResults;
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

  /**
   * Resolves the import path up to this segment through the Go modules of the file: the last segment to the package
   * directory, a leading segment to the module roots it starts.
   */
  @RequiredReadAction
  private ResolveResult[] resolveInGoModules(PsiFile file) {
    FileReferenceSet referenceSet = getFileReferenceSet();
    if (referenceSet.isAbsolutePathReference()
        || referenceSet instanceof GoImportReferenceSet && ((GoImportReferenceSet)referenceSet).isRelativeImport()) {
      return ResolveResult.EMPTY_ARRAY;
    }
    PsiFile originalFile = file.getOriginalFile();
    GoImportPathMap map =
      GoImportPathMap.forFile(file.getProject(), ModuleUtilCore.findModuleForPsiElement(originalFile), originalFile.getVirtualFile());
    if (map == null) {
      return ResolveResult.EMPTY_ARRAY;
    }

    StringBuilder importPath = new StringBuilder();
    FileReference[] references = referenceSet.getAllReferences();
    for (int i = 0; i <= getIndex() && i < references.length; i++) {
      if (i > 0) {
        importPath.append('/');
      }
      importPath.append(references[i].getText());
    }

    PsiManager psiManager = file.getManager();
    VirtualFile directory = map.findDirectory(importPath.toString());
    PsiDirectory psiDirectory = directory != null ? psiManager.findDirectory(directory) : null;
    if (psiDirectory != null) {
      return new ResolveResult[]{new PsiElementResolveResult(psiDirectory)};
    }
    if (isLast()) {
      return ResolveResult.EMPTY_ARRAY;
    }
    List<ResolveResult> results = new ArrayList<>();
    for (VirtualFile moduleRoot : map.findPrefixDirectories(importPath.toString())) {
      PsiDirectory psiModuleRoot = psiManager.findDirectory(moduleRoot);
      if (psiModuleRoot != null) {
        results.add(new PsiElementResolveResult(psiModuleRoot));
      }
    }
    return results.toArray(ResolveResult.EMPTY_ARRAY);
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
  public PsiElement bindToElement(PsiElement element, boolean absolute) throws IncorrectOperationException {
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

  private static LookupElementBuilder createDirectoryLookupElement(PsiDirectory dir) {
    return LookupElementBuilder.createWithSmartPointer(dir.getName(), dir).withIcon(GoIcons.DIRECTORY)
      .withInsertHandler(dir.getFiles().length == 0 ? DIR_INSERT_HANDLER : null);
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
