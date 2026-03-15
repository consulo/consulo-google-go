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
import consulo.localize.LocalizeValue;
import consulo.usage.UsageViewTypeLocation;
import consulo.util.lang.StringUtil;


@ExtensionImpl
public class GoImportUsedAsNameInspection extends GoInspectionBase {
  @Override
  protected GoVisitor buildGoVisitor(ProblemsHolder holder, LocalInspectionToolSession session, Object inspectionState) {
    return new GoVisitor() {
      @Override
      public void visitTypeSpec(GoTypeSpec o) {
        super.visitTypeSpec(o);
        check(o, holder);
      }

      @Override
      public void visitConstDefinition(GoConstDefinition o) {
        super.visitConstDefinition(o);
        check(o, holder);
      }

      @Override
      public void visitFunctionDeclaration(GoFunctionDeclaration o) {
        super.visitFunctionDeclaration(o);
        check(o, holder);
      }

      @Override
      public void visitVarDefinition(GoVarDefinition o) {
        super.visitVarDefinition(o);
        check(o, holder);
      }
    };
  }

  private static void check(GoNamedElement element, ProblemsHolder holder) {
    String name = element.getName();
    if (StringUtil.isNotEmpty(name) &&
        !"_".equals(name) &&
        element.getContainingFile().getImportMap().containsKey(name)) {
      registerProblem(holder, element);
    }
  }

  private static void registerProblem(ProblemsHolder holder, GoNamedElement element) {
    PsiElement identifier = element.getIdentifier();
    if (identifier != null) {
      String elementDescription = ElementDescriptionUtil.getElementDescription(element, UsageViewTypeLocation.INSTANCE);
      String message = StringUtil.capitalize(elementDescription) + " <code>#ref</code> collides with imported package name #loc";
      holder.registerProblem(identifier, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, new GoRenameQuickFix(element));
    }
  }

  @Override
  public LocalizeValue getGroupDisplayName() {
    return LocalizeValue.localizeTODO("Probable bugs");
  }

  @Override
  public LocalizeValue getDisplayName() {
    return LocalizeValue.localizeTODO("Imported package name as name identifier");
  }

  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }
}
