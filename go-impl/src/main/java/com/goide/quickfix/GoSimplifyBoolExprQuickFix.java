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

package com.goide.quickfix;

import com.goide.inspections.GoBoolExpressionsInspection;
import com.goide.psi.GoAndExpr;
import com.goide.psi.GoBinaryExpr;
import com.goide.psi.GoExpression;
import com.goide.psi.GoReferenceExpression;
import com.goide.psi.impl.GoElementFactory;
import com.goide.psi.impl.GoExpressionUtil;
import com.goide.psi.impl.GoPsiImplUtil;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.util.collection.SmartList;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import java.util.List;

public class GoSimplifyBoolExprQuickFix extends LocalQuickFixOnPsiElement {

  public static final String QUICK_FIX_NAME = "Simplify expression";

  public GoSimplifyBoolExprQuickFix(@Nonnull PsiElement element) {
    super(element);
  }

  @Override
  @Nonnull
  public String getFamilyName() {
    return getName();
  }

  @Nonnull
  @Override
  public String getText() {
    return "Simplify expression";
  }

  @Override
  public void invoke(@Nonnull Project project, @Nonnull PsiFile file, @Nonnull PsiElement startElement, @Nonnull PsiElement endElement) {
    if (!(startElement instanceof GoBinaryExpr)) return;
    GoBinaryExpr o = (GoBinaryExpr)startElement;
    boolean and = o instanceof GoAndExpr;

    List<GoExpression> elements = GoBoolExpressionsInspection.collect(o, and);
    List<GoExpression> toRemove = new SmartList<>();
    for (int i = 0; i < elements.size(); i++) {
      GoExpression l = elements.get(i);
      if (l instanceof GoReferenceExpression &&
          (l.textMatches("true") || l.textMatches("false")) &&
          GoPsiImplUtil.builtin(((GoReferenceExpression)l).resolve())) {
        boolean trueExpr = l.textMatches("true");
        if (and ^ !trueExpr) {
          toRemove.add(l);
        }
        else {
          replaceExpressionByBoolConst(o, project, !and);
          return;
        }
      }
      for (int j = i + 1; j < elements.size(); j++) {
        GoExpression r = elements.get(j);
        if (GoBoolExpressionsInspection.isEqualsWithNot(l, r) || GoBoolExpressionsInspection.isEqualsWithNot(r, l)) {
          replaceExpressionByBoolConst(o, project, !and);
        }

        if (GoExpressionUtil.identical(l, r)) toRemove.add(l);
        // todo expr evaluating! x != c1 || x != c2 (c1, c2 const, c1 != c2)
      }
    }

    if (!toRemove.isEmpty()) {
      removeRedundantExpressions(o, project, elements, toRemove, and);
    }
  }

  private static void removeRedundantExpressions(@Nonnull GoBinaryExpr binaryExpr,
                                          @Nonnull Project project,
                                          @Nonnull List<GoExpression> expressions,
                                          @Nonnull List<GoExpression> toRemove,
                                          boolean and) {
    for (GoExpression e : toRemove) {
      expressions.remove(e);
    }
    String separator = and ? " && " : " || ";
    String text = StringUtil.join(expressions, PsiElement::getText, separator);
    GoExpression expression = GoElementFactory.createExpression(project, text);
    binaryExpr.replace(expression);
  }


  private static void replaceExpressionByBoolConst(@Nonnull GoBinaryExpr binaryExpr, @Nonnull Project project, boolean value) {
    GoExpression expression = GoElementFactory.createExpression(project, value ? "true" : "false");
    binaryExpr.replace(expression);
  }
}
