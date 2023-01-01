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
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ContainerUtil;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ExtensionImpl
public class GoDuplicateFieldsOrMethodsInspection extends GoInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder,
                                     @SuppressWarnings({"UnusedParameters", "For future"}) @Nonnull LocalInspectionToolSession session) {
    return new GoVisitor() {
      @Override
      public void visitStructType(@Nonnull GoStructType type) {
        List<GoNamedElement> fields = ContainerUtil.newArrayList();
        type.accept(new GoRecursiveVisitor() {
          @Override
          public void visitFieldDefinition(@Nonnull GoFieldDefinition o) {
            addField(o);
          }

          @Override
          public void visitAnonymousFieldDefinition(@Nonnull GoAnonymousFieldDefinition o) {
            addField(o);
          }

          private void addField(@Nonnull GoNamedElement o) {
            if (!o.isBlank()) fields.add(o);
          }

          @Override
          public void visitType(@Nonnull GoType o) {
            if (o == type) super.visitType(o);
          }
        });
        check(fields, holder, "field");
        super.visitStructType(type);
      }

      @Override
      public void visitInterfaceType(@Nonnull GoInterfaceType o) {
        check(o.getMethodSpecList(), holder, "method");
        super.visitInterfaceType(o);
      }
    };
  }

  private static void check(@Nonnull List<? extends GoNamedElement> fields, @Nonnull ProblemsHolder problemsHolder, @Nonnull String what) {
    Set<String> names = new HashSet<>();
    for (GoCompositeElement field : fields) {
      if (field instanceof GoMethodSpec && ((GoMethodSpec) field).getSignature() == null) {
        // It's an embedded type, not a method or a field.
        continue;
      }
      if (field instanceof GoNamedElement) {
        String name = ((GoNamedElement)field).getName();
        if (names.contains(name)) {
          PsiElement id = ((GoNamedElement)field).getIdentifier();
          problemsHolder.registerProblem(id != null ? id : field, "Duplicate " + what + " <code>#ref</code> #loc",
                                         ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        }
        else {
          ContainerUtil.addIfNotNull(names, name);
        }
      }
    }
  }

  @Nonnull
  @Override
  public String getGroupDisplayName() {
    return "Redeclared symbols";
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Duplicate fields and methods inspection";
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }
}
