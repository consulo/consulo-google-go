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
import com.goide.quickfix.GoRenameQuickFix;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.ElementDescriptionUtil;
import consulo.language.psi.PsiElement;
import consulo.usage.UsageViewTypeLocation;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;

@ExtensionImpl
public class GoImportUsedAsNameInspection extends GoInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session) {
    return new GoVisitor() {
      @Override
      public void visitTypeSpec(@Nonnull GoTypeSpec o) {
        super.visitTypeSpec(o);
        check(o, holder);
      }

      @Override
      public void visitConstDefinition(@Nonnull GoConstDefinition o) {
        super.visitConstDefinition(o);
        check(o, holder);
      }

      @Override
      public void visitFunctionDeclaration(@Nonnull GoFunctionDeclaration o) {
        super.visitFunctionDeclaration(o);
        check(o, holder);
      }

      @Override
      public void visitVarDefinition(@Nonnull GoVarDefinition o) {
        super.visitVarDefinition(o);
        check(o, holder);
      }
    };
  }

  private static void check(@Nonnull GoNamedElement element, @Nonnull ProblemsHolder holder) {
    String name = element.getName();
    if (StringUtil.isNotEmpty(name) &&
        !"_".equals(name) &&
        element.getContainingFile().getImportMap().containsKey(name)) {
      registerProblem(holder, element);
    }
  }

  private static void registerProblem(@Nonnull ProblemsHolder holder, @Nonnull GoNamedElement element) {
    PsiElement identifier = element.getIdentifier();
    if (identifier != null) {
      String elementDescription = ElementDescriptionUtil.getElementDescription(element, UsageViewTypeLocation.INSTANCE);
      String message = StringUtil.capitalize(elementDescription) + " <code>#ref</code> collides with imported package name #loc";
      holder.registerProblem(identifier, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new GoRenameQuickFix(element));
    }
  }

  @Nonnull
  @Override
  public String getGroupDisplayName() {
    return "Probable bugs";
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Imported package name as name identifier";
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }
}
