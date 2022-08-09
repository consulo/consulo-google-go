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
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.highlight.ReadWriteAccessDetector;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;

import javax.annotation.Nonnull;

import static consulo.language.editor.inspection.ProblemHighlightType.WEAK_WARNING;

@ExtensionImpl
public class GoAssignmentToReceiverInspection extends GoInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session) {
    return new GoVisitor() {
      @Override
      public void visitReferenceExpression(@Nonnull GoReferenceExpression o) {
        super.visitReferenceExpression(o);
        if (o.getReadWriteAccess() == ReadWriteAccessDetector.Access.Write) {
          PsiElement resolve = o.resolve();
          if (resolve instanceof GoReceiver) {
            String message = "Assignment to method receiver doesn't propagate to other calls";
            if (((GoReceiver)resolve).getType() instanceof GoPointerType) {
              if (o.getParent() instanceof GoUnaryExpr) {
                GoUnaryExpr p = (GoUnaryExpr)o.getParent();
                if (p.getMul() != null) {
                  // pointer dereference
                  return;
                }
              }
              message = "Assignment to method receiver propagates only to callees but not to callers";
            }
            holder.registerProblem(o, message, WEAK_WARNING);
          }
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
    return "Assignment to receiver";
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }
}