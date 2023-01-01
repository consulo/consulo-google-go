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
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.inspection.*;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.util.AttachmentFactoryUtil;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;

/**
 * Part of the golint tool
 * <p/>
 * https://github.com/golang/lint/blob/32a87160691b3c96046c0c678fe57c5bef761456/lint.go#L827
 */
@ExtensionImpl
public class GoExportedOwnDeclarationInspection extends GoInspectionBase {
  public static final String QUICK_FIX_NAME = "Extract to own declaration";

  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session) {
    return new GoVisitor() {
      @Override
      public void visitConstDeclaration(@Nonnull GoConstDeclaration o) {
        if (o.getParent() instanceof GoFile) {
          for (GoConstSpec spec : o.getConstSpecList()) {
            boolean first = true;
            for (GoConstDefinition constDefinition : spec.getConstDefinitionList()) {
              if (!first && constDefinition.isPublic()) {
                String errorText = "Exported const <code>#ref</code> should have its own declaration #loc";
                holder.registerProblem(constDefinition, errorText, ProblemHighlightType.WEAK_WARNING, new ExtractConstantDefinitionFix());
              }
              first = false;
            }
          }
        }
      }

      @Override
      public void visitVarDeclaration(@Nonnull GoVarDeclaration o) {
        if (o.getParent() instanceof GoFile) {
          for (GoVarSpec spec : o.getVarSpecList()) {
            boolean first = true;
            for (GoVarDefinition varDefinition : spec.getVarDefinitionList()) {
              if (!first && varDefinition.isPublic()) {
                String errorText = "Exported variable <code>#ref</code> should have its own declaration #loc";
                holder.registerProblem(varDefinition, errorText, ProblemHighlightType.WEAK_WARNING, new ExtractVarDefinitionFix());
              }
              first = false;
            }
          }
        }
      }
    };
  }

  @Nonnull
  @Override
  public String getGroupDisplayName() {
    return "Code style issues";
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Exported element should have its own declaration";
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WEAK_WARNING;
  }

  private static class ExtractConstantDefinitionFix extends LocalQuickFixBase {
    private static final Logger LOG = Logger.getInstance(ExtractConstantDefinitionFix.class);

    ExtractConstantDefinitionFix() {
      super(QUICK_FIX_NAME);
    }

    @Override
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
      PsiElement element = descriptor.getPsiElement();
      if (!element.isValid() || !(element instanceof GoConstDefinition)) {
        return;
      }
      String name = ((GoConstDefinition)element).getName();
      if (StringUtil.isEmpty(name)) {
        return;
      }
      GoType type = ((GoConstDefinition)element).findSiblingType();
      GoExpression value = ((GoConstDefinition)element).getValue();
      WriteCommandAction.runWriteCommandAction(project, () -> {
        PsiElement parent = element.getParent();
        PsiElement grandParent = parent != null ? parent.getParent() : null;
        if (parent instanceof GoConstSpec && grandParent instanceof GoConstDeclaration) {
          if (!parent.isValid() || ((GoConstSpec)parent).getConstDefinitionList().indexOf(element) <= 0) {
            return;
          }
          String typeText = type != null ? type.getText() : "";
          String valueText = value != null ? value.getText() : "";
          ((GoConstSpec)parent).deleteDefinition((GoConstDefinition)element);
          if (grandParent.isValid()) {
            ((GoConstDeclaration)grandParent).addSpec(name, typeText, valueText, (GoConstSpec)parent);
            return;
          }
        }
        LOG.error("Cannot run quick fix", AttachmentFactoryUtil.createAttachment(element.getContainingFile().getVirtualFile()));
      });
    }
  }

  private static class ExtractVarDefinitionFix extends LocalQuickFixBase {
    private static final Logger LOG = Logger.getInstance(ExtractVarDefinitionFix.class);

    ExtractVarDefinitionFix() {
      super(QUICK_FIX_NAME);
    }

    @Override
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
      PsiElement element = descriptor.getPsiElement();
      if (!element.isValid() || !(element instanceof GoVarDefinition)) {
        return;
      }
      String name = ((GoVarDefinition)element).getName();
      if (StringUtil.isEmpty(name)) {
        return;
      }
      GoType type = ((GoVarDefinition)element).findSiblingType();
      GoExpression value = ((GoVarDefinition)element).getValue();
      WriteCommandAction.runWriteCommandAction(project, () -> {
        PsiElement parent = element.getParent();
        PsiElement grandParent = parent != null ? parent.getParent() : null;
        if (parent instanceof GoVarSpec && grandParent instanceof GoVarDeclaration) {
          if (!parent.isValid() || ((GoVarSpec)parent).getVarDefinitionList().indexOf(element) <= 0) {
            return;
          }
          String typeText = type != null ? type.getText() : "";
          String valueText = value != null ? value.getText() : "";
          ((GoVarSpec)parent).deleteDefinition((GoVarDefinition)element);
          if (grandParent.isValid()) {
            ((GoVarDeclaration)grandParent).addSpec(name, typeText, valueText, (GoVarSpec)parent);
            return;
          }
        }
        LOG.error("Cannot run quick fix", AttachmentFactoryUtil.createAttachment(element.getContainingFile().getVirtualFile()));
      });
    }
  }
}
