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

import com.goide.psi.GoAnonymousFieldDefinition;
import com.goide.psi.GoVisitor;
import com.goide.quickfix.GoCreateWrapperTypeQuickFix;
import consulo.annotation.component.ExtensionImpl;
import consulo.google.go.inspection.GoGeneralInspectionBase;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemsHolder;

import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;

@ExtensionImpl
public class GoAnonymousFieldDefinitionTypeInspection extends GoGeneralInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder,
                                     @SuppressWarnings({"UnusedParameters", "For future"}) @Nonnull LocalInspectionToolSession session,
                                     Object inspectionState) {
    return new GoVisitor() {
      @Override
      public void visitAnonymousFieldDefinition(@Nonnull GoAnonymousFieldDefinition o) {
        if (o.getTypeReferenceExpression() == null) {
          holder.registerProblem(o, "Invalid type " + o.getType().getText() + ": must be typeName or *typeName",
                                 new GoCreateWrapperTypeQuickFix(o.getType()));
        }
      }
    };
  }

  @Nonnull
  @Override
  public LocalizeValue getDisplayName() {
    return LocalizeValue.localizeTODO("Invalid anonymous field definition type");
  }
}
