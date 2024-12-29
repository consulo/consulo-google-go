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

import com.goide.psi.GoLiteral;
import com.goide.psi.GoStringLiteral;
import com.goide.psi.GoVisitor;
import consulo.annotation.component.ExtensionImpl;
import consulo.google.go.inspection.GoGeneralInspectionBase;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.impl.psi.LeafPsiElement;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class GoInvalidStringOrCharInspection extends GoGeneralInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder,
                                     @SuppressWarnings({"UnusedParameters", "For future"}) @Nonnull LocalInspectionToolSession session,
                                     Object inspectionState) {
    return new GoVisitor() {

      @Override
      public void visitStringLiteral(@Nonnull GoStringLiteral o) {
        PsiElement s = o.getString();
        if (s instanceof LeafPsiElement) {
          int length = ((LeafPsiElement)s).getCachedLength();
          if (length == 1 || ((LeafPsiElement)s).charAt(length - 1) != '"') {
            holder.registerProblem(s, "New line in string");
          }
        }
      }

      @Override
      public void visitLiteral(@Nonnull GoLiteral o) {
        PsiElement c = o.getChar();
        if (c instanceof LeafPsiElement) {
          int length = ((LeafPsiElement)c).getCachedLength();
          if (length == 3 && ((LeafPsiElement)c).charAt(1) == '\'') {
            holder.registerProblem(c, "Empty character literal or unescaped ' in character literal");
          }
          if (length < 3) {
            holder.registerProblem(c, "Missing '");
          }
        }
      }
    };
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Invalid strings and runes";
  }
}
