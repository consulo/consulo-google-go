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

package com.goide.inspections;

import com.goide.codeInsight.imports.GoImportOptimizer;
import com.goide.psi.GoFile;
import com.goide.psi.GoImportSpec;
import com.goide.psi.GoRecursiveVisitor;
import com.goide.psi.impl.GoElementFactory;
import com.goide.quickfix.GoRenameQuickFix;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationManager;
import consulo.find.FindManager;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.inspection.*;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiReference;
import consulo.project.Project;
import consulo.util.collection.MultiMap;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.*;

@ExtensionImpl
public class GoUnusedImportInspection extends GoInspectionBase {
  @Nullable
  private final static LocalQuickFix OPTIMIZE_QUICK_FIX = new LocalQuickFixBase("Optimize imports") {
    @Override
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
      PsiElement element = descriptor.getPsiElement();
      if (element == null) {
        return;
      }
      PsiFile file = element.getContainingFile();
      WriteCommandAction.runWriteCommandAction(project, new GoImportOptimizer().processFile(file));
    }
  };

  @Nullable private final static LocalQuickFix IMPORT_FOR_SIDE_EFFECTS_QUICK_FIX = new LocalQuickFixBase("Import for side-effects") {
    @Override
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
      PsiElement element = descriptor.getPsiElement();
      if (!(element instanceof GoImportSpec)) {
        return;
      }
      element.replace(GoElementFactory.createImportSpec(project, ((GoImportSpec)element).getPath(), "_"));
    }
  };

  private static void resolveAllReferences(@Nonnull GoFile file) {
    file.accept(new GoRecursiveVisitor() {
      @Override
      public void visitElement(@Nonnull PsiElement o) {
        for (PsiReference reference : o.getReferences()) {
          reference.resolve();
        }
      }
    });
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Unused import inspection";
  }

  @Nonnull
  @Override
  public String getGroupDisplayName() {
    return "Declaration redundancy";
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

  @Override
  protected void checkFile(@Nonnull GoFile file, @Nonnull ProblemsHolder problemsHolder) {
    MultiMap<String, GoImportSpec> importMap = file.getImportMap();

    for (PsiElement importIdentifier : GoImportOptimizer.findRedundantImportIdentifiers(importMap)) {
      problemsHolder.registerProblem(importIdentifier, "Redundant alias", ProblemHighlightType.LIKE_UNUSED_SYMBOL, OPTIMIZE_QUICK_FIX);
    }

    Set<GoImportSpec> duplicatedEntries = GoImportOptimizer.findDuplicatedEntries(importMap);
    for (GoImportSpec duplicatedImportSpec : duplicatedEntries) {
      problemsHolder.registerProblem(duplicatedImportSpec, "Redeclared import", ProblemHighlightType.GENERIC_ERROR, OPTIMIZE_QUICK_FIX);
    }

    for (Map.Entry<String, Collection<GoImportSpec>> specs : importMap.entrySet()) {
      Iterator<GoImportSpec> imports = specs.getValue().iterator();
      GoImportSpec originalImport = imports.next();
      if (originalImport.isDot() || originalImport.isForSideEffects()) {
        continue;
      }
      while (imports.hasNext()) {
        GoImportSpec redeclaredImport = imports.next();
        if (!duplicatedEntries.contains(redeclaredImport)) {
          LocalQuickFix[] quickFixes = FindManager.getInstance(redeclaredImport.getProject()).canFindUsages(redeclaredImport)
                                       ? new LocalQuickFix[]{new GoRenameQuickFix(redeclaredImport)}
                                       : LocalQuickFix.EMPTY_ARRAY;
          problemsHolder.registerProblem(redeclaredImport, "Redeclared import", ProblemHighlightType.GENERIC_ERROR, quickFixes);
        }
      }
    }

    if (importMap.containsKey(".")) {
      if (!problemsHolder.isOnTheFly() || ApplicationManager.getApplication().isUnitTestMode()) resolveAllReferences(file);
    }
    MultiMap<String, GoImportSpec> unusedImportsMap = GoImportOptimizer.filterUnusedImports(file, importMap);
    Set<GoImportSpec> unusedImportSpecs = new HashSet<>(unusedImportsMap.values());
    for (PsiElement importEntry : unusedImportSpecs) {
      GoImportSpec spec = GoImportOptimizer.getImportSpec(importEntry);
      if (spec != null && spec.getImportString().resolve() != null) {
        problemsHolder.registerProblem(spec, "Unused import", ProblemHighlightType.GENERIC_ERROR, OPTIMIZE_QUICK_FIX,
                                       IMPORT_FOR_SIDE_EFFECTS_QUICK_FIX);
      }
    }
  }
}
