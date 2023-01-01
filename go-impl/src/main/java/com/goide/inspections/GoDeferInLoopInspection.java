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

import com.goide.psi.GoDeferStatement;
import com.goide.psi.GoForStatement;
import com.goide.psi.GoFunctionLit;
import com.goide.psi.GoVisitor;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.util.PsiTreeUtil;

import javax.annotation.Nonnull;

@ExtensionImpl
public class GoDeferInLoopInspection extends GoInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session) {
    return new GoVisitor() {
      @Override
      public void visitDeferStatement(@Nonnull GoDeferStatement o) {
        if (PsiTreeUtil.getParentOfType(o, GoForStatement.class, GoFunctionLit.class) instanceof GoForStatement) {
          holder.registerProblem(o.getDefer(), "Possible resource leak, 'defer' is called in a for loop.",
                                 ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        }
      }
    };
  }

  @Nonnull
  @Override
  public String getGroupDisplayName() {
    return "Control flow issues";
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Defer in loop";
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }
}
