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

package com.goide.completion;

import com.goide.GoFileType;
import com.goide.project.GoExcludedPathsSettings;
import com.goide.project.GoVendoringUtil;
import com.goide.psi.GoFile;
import com.goide.psi.GoImportString;
import com.goide.psi.impl.GoPsiImplUtil;
import com.goide.runconfig.testing.GoTestFinder;
import com.goide.util.GoUtil;
import consulo.application.progress.ProgressManager;
import consulo.document.util.TextRange;
import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionProvider;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.FileTypeIndex;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ModuleUtilCore;
import consulo.language.util.ProcessingContext;
import consulo.module.Module;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GoImportPathsCompletionProvider implements CompletionProvider {
  @Override
  public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result) {
    GoImportString importString = PsiTreeUtil.getParentOfType(parameters.getPosition(), GoImportString.class);
    if (importString == null) return;
    String path = importString.getPath();
    if (path.startsWith("./") || path.startsWith("../")) return;

    TextRange pathRange = importString.getPathTextRange().shiftRight(importString.getTextRange().getStartOffset());
    String newPrefix = parameters.getEditor().getDocument().getText(TextRange.create(pathRange.getStartOffset(), parameters.getOffset()));
    result = result.withPrefixMatcher(result.getPrefixMatcher().cloneWithPrefix(newPrefix));

    Module module = ModuleUtilCore.findModuleForPsiElement(parameters.getOriginalFile());
    if (module != null) {
      addCompletions(result, module, parameters.getOriginalFile(), GoUtil.goPathResolveScope(module, parameters.getOriginalFile()), false);
    }
  }

  public static void addCompletions(@Nonnull CompletionResultSet result,
                                    @Nonnull Module module,
                                    @Nullable PsiElement context,
                                    @Nonnull GlobalSearchScope scope,
                                    boolean allowMain) {
    Project project = module.getProject();
    boolean vendoringEnabled = GoVendoringUtil.isVendoringEnabled(module);
    String contextImportPath = GoCompletionUtil.getContextImportPath(context, vendoringEnabled);
    GoExcludedPathsSettings excludedSettings = GoExcludedPathsSettings.getInstance(project);
    PsiFile contextFile = context != null ? context.getContainingFile() : null;
    boolean testFileWithTestPackage = GoTestFinder.isTestFileWithTestPackage(contextFile);
    for (VirtualFile file : FileTypeIndex.getFiles(GoFileType.INSTANCE, scope)) {
      ProgressManager.checkCanceled();
      PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
      if (!(psiFile instanceof GoFile)) continue;
      
      PsiDirectory directory = psiFile.getContainingDirectory();
      if (directory == null) continue;

      GoFile goFile = (GoFile)psiFile;
      if (!GoPsiImplUtil.canBeAutoImported(goFile, allowMain, module)) continue;
      
      String importPath = goFile.getImportPath(vendoringEnabled);
      if (StringUtil.isNotEmpty(importPath) && !excludedSettings.isExcluded(importPath)
          && (testFileWithTestPackage || !importPath.equals(contextImportPath))) {
        result.addElement(GoCompletionUtil.createPackageLookupElement(importPath, contextImportPath, directory, false));
      }
    }
  }
}
