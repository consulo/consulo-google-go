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
import com.goide.psi.impl.GoPsiImplUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.google.go.inspection.GoGeneralInspectionBase;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.util.collection.ContainerUtil;

import jakarta.annotation.Nonnull;
import java.util.List;

@ExtensionImpl
public class GoFunctionCallInspection extends GoGeneralInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session, Object inspectionState) {
    return new GoVisitor() {
      @Override
      public void visitCallExpr(@Nonnull GoCallExpr o) {
        super.visitCallExpr(o);
        PsiElement resolve = GoPsiImplUtil.resolveCallRaw(o);
        GoExpression expression = o.getExpression();
        if (resolve != null && expression instanceof GoReferenceExpression) {
          List<GoExpression> list = o.getArgumentList().getExpressionList();
          int actualSize = list.size();
          if (resolve instanceof GoTypeSpec && actualSize != 1) {
            String message = String.format("%sto conversion to %s: %s.", actualSize == 0 ? "Missing argument " : "Too many arguments ",
                                           expression.getText(), o.getText());
            holder.registerProblem(o, message);
          }
          else if (resolve instanceof GoSignatureOwner) {
            GoSignature signature = ((GoSignatureOwner)resolve).getSignature();
            if (signature == null) return;
            int expectedSize = 0;
            GoParameters parameters = signature.getParameters();
            for (GoParameterDeclaration declaration : parameters.getParameterDeclarationList()) {
              if (declaration.isVariadic() && actualSize >= expectedSize) return;
              int size = declaration.getParamDefinitionList().size();
              expectedSize += size == 0 ? 1 : size;
            }

            if (actualSize == 1) {
              GoExpression first = ContainerUtil.getFirstItem(list);
              GoSignatureOwner firstResolve = GoPsiImplUtil.resolveCall(first);
              if (firstResolve != null) {
                actualSize = GoInspectionUtil.getFunctionResultCount(firstResolve);
              }
            }

            GoReferenceExpression qualifier = ((GoReferenceExpression)expression).getQualifier();
            boolean isMethodExpr = qualifier != null && qualifier.resolve() instanceof GoTypeSpec;
            if (isMethodExpr) actualSize -= 1; // todo: a temp workaround for method specs

            if (actualSize == expectedSize) return;

            String tail = " arguments in call to " + expression.getText();
            holder.registerProblem(o.getArgumentList(), (actualSize > expectedSize ? "too many" : "not enough") + tail);
          }
        }
      }
    };
  }

  @Nonnull
  @Override
  public LocalizeValue getDisplayName() {
    return LocalizeValue.localizeTODO("Function call inspection");
  }
}
