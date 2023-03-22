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

import com.goide.psi.*;
import com.goide.quickfix.GoReplaceWithSelectStatementQuickFix;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ExtensionImpl
public class GoInfiniteForInspection extends GoInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session, Object inspectionState) {
    return new GoVisitor() {
      @Override
      public void visitForStatement(@Nonnull GoForStatement o) {
        super.visitForStatement(o);

        if (o.getExpression() == null &&
            isEmpty(o.getBlock()) &&
            !hasRangeClause(o.getRangeClause()) &&
            !hasClause(o.getForClause())) {
          holder.registerProblem(o, "Infinite for loop", ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new GoReplaceWithSelectStatementQuickFix());
        }
      }
    };
  }

  private static boolean isEmpty(@Nullable GoBlock block) {
    return block != null && block.getStatementList().isEmpty();
  }

  private static boolean hasRangeClause(@Nullable GoRangeClause rangeClause) {
    return rangeClause != null && !rangeClause.getExpressionList().isEmpty();
  }

  private static boolean hasClause(@Nullable GoForClause forClause) {
    return forClause != null && !forClause.getStatementList().isEmpty();
  }

  @Nonnull
  @Override
  public String getGroupDisplayName() {
    return "Control flow issues";
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Infinite for loop";
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }
}
