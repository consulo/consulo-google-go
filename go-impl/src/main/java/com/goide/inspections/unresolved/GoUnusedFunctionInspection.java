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

package com.goide.inspections.unresolved;

import com.goide.GoConstants;
import com.goide.inspections.GoInspectionBase;
import com.goide.psi.GoFile;
import com.goide.psi.GoFunctionDeclaration;
import com.goide.psi.GoVisitor;
import com.goide.quickfix.GoDeleteQuickFix;
import com.goide.quickfix.GoRenameToBlankQuickFix;
import com.goide.runconfig.testing.GoTestFinder;
import com.goide.runconfig.testing.GoTestFunctionType;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.search.ReferencesSearch;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;

@ExtensionImpl
public class GoUnusedFunctionInspection extends GoInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session) {
    return new GoVisitor() {
      @Override
      public void visitFunctionDeclaration(@Nonnull GoFunctionDeclaration o) {
        if (o.isBlank()) return;
        GoFile file = o.getContainingFile();
        String name = o.getName();
        if (!canRun(name)) return;
        if (GoConstants.MAIN.equals(file.getPackageName()) && GoConstants.MAIN.equals(name)) return;
        if (GoConstants.INIT.equals(name)) return;
        if (GoTestFinder.isTestFile(file) && GoTestFunctionType.fromName(name) != null) return;
        if (ReferencesSearch.search(o, o.getUseScope()).findFirst() == null) {
          PsiElement id = o.getIdentifier();
          TextRange range = TextRange.from(id.getStartOffsetInParent(), id.getTextLength());
          holder.registerProblem(o, "Unused function <code>#ref</code> #loc", ProblemHighlightType.LIKE_UNUSED_SYMBOL, range,
              new GoDeleteQuickFix("Delete function", GoFunctionDeclaration.class), new GoRenameToBlankQuickFix(o));
        }
      }
    };
  }

  protected boolean canRun(String name) {
    return !StringUtil.isCapitalized(name);
  }

  @Nonnull
  @Override
  public String getGroupDisplayName() {
    return "Declaration redundancy";
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Unused function inspection";
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }
}
