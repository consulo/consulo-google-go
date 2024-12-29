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

import com.goide.psi.GoReferenceExpression;
import com.goide.psi.GoVisitor;
import consulo.annotation.component.ExtensionImpl;
import consulo.google.go.inspection.GoGeneralInspectionBase;
import consulo.language.editor.highlight.ReadWriteAccessDetector;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class GoUnderscoreUsedAsValueInspection extends GoGeneralInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session, Object inspectionState) {
    return new GoVisitor() {
      @Override
      public void visitReferenceExpression(@Nonnull GoReferenceExpression o) {
        super.visitReferenceExpression(o);
        if (o.getIdentifier().textMatches("_") && o.getReadWriteAccess() != ReadWriteAccessDetector.Access.Write) {
          holder.registerProblem(o, "Cannot use <code>#ref</code> as value #loc", ProblemHighlightType.ERROR);
        }
      }
    };
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Underscore used as value";
  }
}
