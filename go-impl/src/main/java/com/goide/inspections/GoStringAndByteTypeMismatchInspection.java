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

import com.goide.GoTypes;
import com.goide.psi.*;
import com.goide.psi.impl.GoPsiImplUtil;
import com.goide.psi.impl.GoTypeUtil;
import com.goide.quickfix.GoConvertStringToByteQuickFix;
import consulo.annotation.component.ExtensionImpl;
import consulo.google.go.inspection.GoGeneralInspectionBase;
import consulo.language.ast.TokenSet;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.Trinity;

import javax.annotation.Nonnull;
import java.util.Arrays;

@ExtensionImpl
public class GoStringAndByteTypeMismatchInspection extends GoGeneralInspectionBase {
  private static final String TEXT_HINT = "Mismatched types: byte and string";
  private static final GoConvertStringToByteQuickFix STRING_INDEX_IS_BYTE_QUICK_FIX = new GoConvertStringToByteQuickFix();

  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session) {
    return new GoVisitor() {

      @Override
      public void visitConditionalExpr(@Nonnull GoConditionalExpr o) {
        GoExpression left = o.getLeft();
        GoExpression right = o.getRight();

        GoIndexOrSliceExpr indexExpr = ContainerUtil.findInstance(Arrays.asList(left, right), GoIndexOrSliceExpr.class);
        GoStringLiteral stringLiteral = ContainerUtil.findInstance(Arrays.asList(left, right), GoStringLiteral.class);

        if (indexExpr == null || stringLiteral == null) {
          return;
        }

        if (isStringIndexExpression(indexExpr)) {
          LocalQuickFix[] fixes = GoPsiImplUtil.isSingleCharLiteral(stringLiteral) ? new LocalQuickFix[]{STRING_INDEX_IS_BYTE_QUICK_FIX}
                                                                                   : LocalQuickFix.EMPTY_ARRAY;
          holder.registerProblem(o, TEXT_HINT, ProblemHighlightType.GENERIC_ERROR, fixes);
        }
      }
    };
  }

  private static boolean isStringIndexExpression(@Nonnull GoIndexOrSliceExpr expr) {
    GoExpression expression = expr.getExpression();
    if (expression == null || !GoTypeUtil.isString(expression.getGoType(null))) {
      return false;
    }

    Trinity<GoExpression, GoExpression, GoExpression> indices = expr.getIndices();
    return indices.getSecond() == null
           && indices.getThird() == null
           && expr.getNode().getChildren(TokenSet.create(GoTypes.COLON)).length == 0;
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Mismatched types: byte and string";
  }
}
