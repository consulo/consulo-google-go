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
import com.goide.psi.impl.GoElementFactory;
import com.goide.psi.impl.GoPsiImplUtil;
import com.goide.util.GoUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.progress.ProgressManager;
import consulo.language.editor.inspection.*;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@ExtensionImpl
public class GoStructInitializationInspection extends GoInspectionBase<GoStructInitializationInspectionState> {
  public static final String REPLACE_WITH_NAMED_STRUCT_FIELD_FIX_NAME = "Replace with named struct field";

  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder,
                                     @Nonnull LocalInspectionToolSession session,
                                     GoStructInitializationInspectionState state) {
    return new GoVisitor() {
      @Override
      public void visitLiteralValue(@Nonnull GoLiteralValue o) {
        if (PsiTreeUtil.getParentOfType(o, GoReturnStatement.class, GoShortVarDeclaration.class, GoAssignmentStatement.class) == null) {
          return;
        }
        PsiElement parent = o.getParent();
        GoType refType = GoPsiImplUtil.getLiteralType(parent, false);
        if (refType instanceof GoStructType) {
          processStructType(holder, o, (GoStructType)refType, state);
        }
      }
    };
  }

  @Nonnull
  @Override
  public InspectionToolState<?> createStateProvider() {
    return new GoStructInitializationInspectionState();
  }

  @Nonnull
  @Override
  public String getGroupDisplayName() {
    return "Code style issues";
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Struct initialization without field names";
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WEAK_WARNING;
  }

  private void processStructType(@Nonnull ProblemsHolder holder,
                                 @Nonnull GoLiteralValue element,
                                 @Nonnull GoStructType structType,
                                 GoStructInitializationInspectionState state) {
    if (state.reportLocalStructs || !GoUtil.inSamePackage(structType.getContainingFile(), element.getContainingFile())) {
      processLiteralValue(holder, element, structType.getFieldDeclarationList());
    }
  }

  private static void processLiteralValue(@Nonnull ProblemsHolder holder,
                                          @Nonnull GoLiteralValue o,
                                          @Nonnull List<GoFieldDeclaration> fields) {
    List<GoElement> vals = o.getElementList();
    for (int elemId = 0; elemId < vals.size(); elemId++) {
      ProgressManager.checkCanceled();
      GoElement element = vals.get(elemId);
      if (element.getKey() == null && elemId < fields.size()) {
        String structFieldName = getFieldName(fields.get(elemId));
        LocalQuickFix[] fixes = structFieldName != null ? new LocalQuickFix[]{new GoReplaceWithNamedStructFieldQuickFix(structFieldName)}
                                                        : LocalQuickFix.EMPTY_ARRAY;
        holder.registerProblem(element, "Unnamed field initialization", ProblemHighlightType.GENERIC_ERROR_OR_WARNING, fixes);
      }
    }
  }

  @Nullable
  private static String getFieldName(@Nonnull GoFieldDeclaration declaration) {
    List<GoFieldDefinition> list = declaration.getFieldDefinitionList();
    GoFieldDefinition fieldDefinition = ContainerUtil.getFirstItem(list);
    return fieldDefinition != null ? fieldDefinition.getIdentifier().getText() : null;
  }

  private static class GoReplaceWithNamedStructFieldQuickFix extends LocalQuickFixBase {
    private String myStructField;

    public GoReplaceWithNamedStructFieldQuickFix(@Nonnull String structField) {
      super(REPLACE_WITH_NAMED_STRUCT_FIELD_FIX_NAME);
      myStructField = structField;
    }

    @Override
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
      PsiElement startElement = descriptor.getStartElement();
      if (startElement instanceof GoElement) {
        startElement.replace(GoElementFactory.createLiteralValueElement(project, myStructField, startElement.getText()));
      }
    }
  }
}
