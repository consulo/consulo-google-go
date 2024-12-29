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
import com.goide.quickfix.GoDeleteAmpersandAndTypeInCompositeLitQuickFix;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.CleanupLocalInspectionTool;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class GoRedundantTypeDeclInCompositeLit extends GoInspectionBase implements CleanupLocalInspectionTool {
  public final static String DELETE_TYPE_DECLARATION_QUICK_FIX_NAME = "Delete redundant type declaration";
  private static final GoDeleteAmpersandAndTypeInCompositeLitQuickFix QUICK_FIX = new GoDeleteAmpersandAndTypeInCompositeLitQuickFix();

  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session, Object inspectionState) {
    return new GoVisitor() {
      @Override
      public void visitCompositeLit(@Nonnull GoCompositeLit o) {
        GoLiteralValue literalValue = o.getLiteralValue();
        if (literalValue == null) return;

        GoType expectedType = getExpectedType(o);

        // TODO o.getType() instanceof GoStruct (struct or T[][])

        if (expectedType != null) {
          for (GoElement element : literalValue.getElementList()) {
            GoValue elementValue = element.getValue();
            if (elementValue != null) {
              GoExpression expr = elementValue.getExpression();
              if (expectedType instanceof GoPointerType && expr instanceof GoUnaryExpr) {
                GoUnaryExpr unaryExpr = (GoUnaryExpr)expr;
                PsiElement bitAnd = unaryExpr.getBitAnd();
                if (bitAnd != null && unaryExpr.getExpression() instanceof GoCompositeLit) {
                  GoCompositeLit compositeLit = (GoCompositeLit)unaryExpr.getExpression();
                  if (isTypeReferencesEquals(((GoPointerType)expectedType).getType(), compositeLit)) {
                    GoTypeReferenceExpression typeExpr = compositeLit.getTypeReferenceExpression();
                    if (typeExpr != null) {
                      holder.registerProblem(holder.getManager().createProblemDescriptor(bitAnd, typeExpr, "Redundant type declaration",
                                                                                         ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                                                                                         holder.isOnTheFly(), QUICK_FIX));
                    }
                  }
                }
              }
              else if (expr instanceof GoCompositeLit && isTypeReferencesEquals(expectedType, (GoCompositeLit)expr)) {
                GoTypeReferenceExpression typeExpr = ((GoCompositeLit)expr).getTypeReferenceExpression();
                if (typeExpr != null) {
                  holder.registerProblem(typeExpr, "Redundant type declaration", ProblemHighlightType.LIKE_UNUSED_SYMBOL, QUICK_FIX);
                }
              }
            }
          }
        }
      }
    };
  }

  @Nullable
  private static GoType getExpectedType(@Nonnull GoCompositeLit o) {
    if (o.getType() instanceof GoArrayOrSliceType && ((GoArrayOrSliceType)o.getType()).getType() != null) {
      return ((GoArrayOrSliceType)o.getType()).getType();
    }
    if (o.getType() instanceof GoMapType && ((GoMapType)o.getType()).getValueType() != null) {
      return ((GoMapType)o.getType()).getValueType();
    }
    return null;
  }

  private static boolean isTypeReferencesEquals(@Nullable GoType pattern, @Nonnull GoCompositeLit value) {
    if (pattern == null || !pattern.isValid() || !value.isValid()) {
      return false;
    }

    if (pattern.getTypeReferenceExpression() == null || value.getTypeReferenceExpression() == null) {
      return false;
    }

    if (pattern.getTypeReferenceExpression().resolve() != value.getTypeReferenceExpression().resolve()) {
      return false;
    }
    //TODO Complex type comparison
    return true;
  }

  @Nonnull
  @Override
  public String getGroupDisplayName() {
    return "Declaration redundancy";
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Redundant type declaration in composite literals";
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }
}