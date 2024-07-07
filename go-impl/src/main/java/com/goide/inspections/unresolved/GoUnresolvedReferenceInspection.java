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

package com.goide.inspections.unresolved;

import com.goide.GoTypes;
import com.goide.codeInsight.imports.GoImportPackageQuickFix;
import com.goide.psi.*;
import com.goide.psi.impl.GoReference;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.google.go.inspection.GoGeneralInspectionBase;
import consulo.language.ast.ASTNode;
import consulo.language.codeStyle.FormatterUtil;
import consulo.language.editor.highlight.ReadWriteAccessDetector;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.path.FileReference;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.ContainerUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import static consulo.language.editor.inspection.ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
import static consulo.language.editor.inspection.ProblemHighlightType.LIKE_UNKNOWN_SYMBOL;

@ExtensionImpl
public class GoUnresolvedReferenceInspection extends GoGeneralInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session, Object inspectionState) {
    return new GoVisitor() {
      @Override
      public void visitFieldName(@Nonnull GoFieldName o) {
        super.visitFieldName(o);
        PsiElement resolve = o.resolve();
        if (resolve == null) {
          PsiElement id = o.getIdentifier();
          holder.registerProblem(id, "Unknown field <code>#ref</code> in struct literal #loc", LIKE_UNKNOWN_SYMBOL);
        }
      }

      @Override
      public void visitReferenceExpression(@Nonnull GoReferenceExpression o) {
        super.visitReferenceExpression(o);
        GoReference reference = o.getReference();
        GoReferenceExpression qualifier = o.getQualifier();
        GoReference qualifierRef = qualifier != null ? qualifier.getReference() : null;
        PsiElement qualifierResolve = qualifierRef != null ? qualifierRef.resolve() : null;
        if (qualifier != null && qualifierResolve == null) return;
        ResolveResult[] results = reference.multiResolve(false);
        PsiElement id = o.getIdentifier();
        String name = id.getText();
        if (results.length > 1) {
          holder.registerProblem(id, "Ambiguous reference " + "'" + name + "'", GENERIC_ERROR_OR_WARNING);
        }
        else if (reference.resolve() == null) {
          LocalQuickFix[] fixes = LocalQuickFix.EMPTY_ARRAY;
          if (isProhibited(o, qualifier)) {
            fixes = createImportPackageFixes(o, reference, holder.isOnTheFly());
          }
          else if (holder.isOnTheFly()) {
            boolean canBeLocal = PsiTreeUtil.getParentOfType(o, GoBlock.class) != null;
            List<LocalQuickFix> fixesList = ContainerUtil.newArrayList(new GoIntroduceGlobalVariableFix(id, name));
            if (canBeLocal) {
              fixesList.add(new GoIntroduceLocalVariableFix(id, name));
            }
            PsiElement parent = o.getParent();
            if (o.getReadWriteAccess() == ReadWriteAccessDetector.Access.Read) {
              fixesList.add(new GoIntroduceGlobalConstantFix(id, name));
              if (canBeLocal) {
                fixesList.add(new GoIntroduceLocalConstantFix(id, name));
              }
            }
            else if (canBeLocal) {
              PsiElement grandParent = parent.getParent();
              if (grandParent instanceof GoAssignmentStatement) {
                fixesList.add(new GoReplaceAssignmentWithDeclarationQuickFix(grandParent));
              }
              else if (parent instanceof GoRangeClause || parent instanceof GoRecvStatement) {
                fixesList.add(new GoReplaceAssignmentWithDeclarationQuickFix(parent));
              }
            }

            if (parent instanceof GoCallExpr && PsiTreeUtil.getParentOfType(o, GoConstDeclaration.class) == null) {
              fixesList.add(new GoIntroduceFunctionFix(parent, name));
            }
            fixes = fixesList.toArray(new LocalQuickFix[fixesList.size()]);
          }
          holder.registerProblem(id, "Unresolved reference " + "'" + name + "'", LIKE_UNKNOWN_SYMBOL, fixes);
        }
      }

      @Override
      @RequiredReadAction
      public void visitImportSpec(@Nonnull GoImportSpec o) {
        if (o.isCImport()) return;
        GoImportString string = o.getImportString();
        if (string.getTextLength() < 2) return;
        PsiReference[] references = string.getReferences();
        for (PsiReference reference : references) {
          if (reference instanceof FileReference) {
            ResolveResult[] resolveResults = ((FileReference)reference).multiResolve(false);
            if (resolveResults.length == 0) {
              ProblemHighlightType type = reference.getRangeInElement().isEmpty() ? GENERIC_ERROR_OR_WARNING : LIKE_UNKNOWN_SYMBOL;
              holder.registerProblem(reference, ProblemsHolder.unresolvedReferenceMessage(reference).get(), type);
            }
          }
        }
      }

      @Override
      public void visitLabelRef(@Nonnull GoLabelRef o) {
        PsiReference reference = o.getReference();
        String name = o.getText();
        if (reference.resolve() == null) {
          holder.registerProblem(o, "Unresolved label " + "'" + name + "'", LIKE_UNKNOWN_SYMBOL);
        }
      }

      @Override
      public void visitTypeReferenceExpression(@Nonnull GoTypeReferenceExpression o) {
        super.visitTypeReferenceExpression(o);
        PsiReference reference = o.getReference();
        GoTypeReferenceExpression qualifier = o.getQualifier();
        PsiElement qualifierResolve = qualifier != null ? qualifier.resolve() : null;
        if (qualifier != null && qualifierResolve == null) return;
        if (reference.resolve() == null) {
          PsiElement id = o.getIdentifier();
          String name = id.getText();
          boolean isProhibited = isProhibited(o, qualifier);
          LocalQuickFix[] fixes = LocalQuickFix.EMPTY_ARRAY;
          if (isProhibited) {
            fixes = createImportPackageFixes(o, reference, holder.isOnTheFly());
          }
          else if (holder.isOnTheFly()) {
            fixes = new LocalQuickFix[]{new GoIntroduceTypeFix(id, name)};
          }
          holder.registerProblem(id, "Unresolved type " + "'" + name + "'", LIKE_UNKNOWN_SYMBOL, fixes);
        }
      }
    };
  }

  @Nonnull
  private static LocalQuickFix[] createImportPackageFixes(@Nonnull PsiElement target, @Nonnull PsiReference reference, boolean onTheFly) {
    if (onTheFly) {
      GoImportPackageQuickFix importFix = new GoImportPackageQuickFix(reference);
      if (importFix.isAvailable(target.getProject(), target.getContainingFile(), target, target)) {
        return new LocalQuickFix[]{importFix};
      }
    }
    else {
      List<String> packagesToImport = GoImportPackageQuickFix.getImportPathVariantsToImport(reference.getCanonicalText(), target);
      if (!packagesToImport.isEmpty()) {
        Collection<LocalQuickFix> result = ContainerUtil.newArrayList();
        for (String importPath : packagesToImport) {
          GoImportPackageQuickFix importFix = new GoImportPackageQuickFix(target, importPath);
          if (importFix.isAvailable(target.getProject(), target.getContainingFile(), target, target)) {
            result.add(importFix);
          }
        }
        return result.toArray(new LocalQuickFix[result.size()]);
      }
    }
    return LocalQuickFix.EMPTY_ARRAY;
  }

  private static boolean isProhibited(@Nonnull GoCompositeElement o, @Nullable GoCompositeElement qualifier) {
    ASTNode next = FormatterUtil.getNextNonWhitespaceSibling(o.getNode());
    boolean isDot = next != null && next.getElementType() == GoTypes.DOT;
    return isDot || qualifier != null;
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Unresolved reference inspection";
  }
}