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

package com.goide.intentions;

import com.goide.psi.*;
import com.goide.psi.impl.GoElementFactory;
import com.goide.psi.impl.GoPsiImplUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.editor.intention.BaseElementAtCaretIntentionAction;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.util.collection.MultiMap;
import consulo.util.lang.ObjectUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.Contract;

import java.util.List;
import java.util.Set;

import static consulo.util.collection.ContainerUtil.*;

@ExtensionImpl
@IntentionMetaData(ignoreId = "go.move.to.struct.initializer", fileExtensions = "go", categories = "Go")
public class GoMoveToStructInitializationIntention extends BaseElementAtCaretIntentionAction {
  public static final LocalizeValue NAME = LocalizeValue.localizeTODO("Move field assignment to struct initialization");

  public GoMoveToStructInitializationIntention() {
    setText(NAME);
  }

  @Override
  public boolean isAvailable(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) {
    return getData(element) != null;
  }

  @Nullable
  private static Data getData(@Nonnull PsiElement element) {
    if (!element.isValid() || !element.isWritable()) return null;
    GoAssignmentStatement assignment = getValidAssignmentParent(element);
    GoReferenceExpression selectedFieldReference = assignment != null ? getFieldReferenceExpression(element, assignment) : null;
    GoCompositeLit compositeLit = selectedFieldReference != null ? getStructLiteralByReference(selectedFieldReference, assignment) : null;
    if (compositeLit == null) return null;

    List<GoReferenceExpression> references = getUninitializedSingleFieldReferences(assignment, selectedFieldReference, compositeLit);
    return !references.isEmpty() ? new Data(assignment, compositeLit, references) : null;
  }

  @Nullable
  private static GoAssignmentStatement getValidAssignmentParent(@Nullable PsiElement element) {
    GoAssignmentStatement assignment = PsiTreeUtil.getNonStrictParentOfType(element, GoAssignmentStatement.class);
    return assignment != null && assignment.isValid() && getLeftHandElements(assignment).size() == assignment.getExpressionList().size()
           ? assignment : null;
  }

  @Nullable
  private static GoReferenceExpression getFieldReferenceExpression(@Nonnull PsiElement selectedElement,
                                                                   @Nonnull GoAssignmentStatement assignment) {
    GoReferenceExpression selectedReferenceExpression = PsiTreeUtil.getTopmostParentOfType(selectedElement, GoReferenceExpression.class);
    if (isFieldReferenceExpression(selectedReferenceExpression)) {
      return !isAssignedInPreviousStatement(selectedReferenceExpression, assignment) ? selectedReferenceExpression : null;
    }

    List<GoReferenceExpression> fieldReferenceExpressions = getFieldReferenceExpressions(assignment);
    if (exists(fieldReferenceExpressions, expression -> isAssignedInPreviousStatement(expression, assignment))) return null;

    Set<PsiElement> resolvedFields = map2Set(fieldReferenceExpressions, GoMoveToStructInitializationIntention::resolveQualifier);
    return resolvedFields.size() == 1 ? getFirstItem(fieldReferenceExpressions) : null;
  }

  @Nonnull
  private static List<GoReferenceExpression> getFieldReferenceExpressions(@Nonnull GoAssignmentStatement assignment) {
    return filter(map(getLeftHandElements(assignment), GoMoveToStructInitializationIntention::unwrapParensAndCast),
                  GoMoveToStructInitializationIntention::isFieldReferenceExpression);
  }

  @Nullable
  private static GoReferenceExpression unwrapParensAndCast(@Nullable PsiElement e) {
    while (e instanceof GoParenthesesExpr) {
      e = ((GoParenthesesExpr)e).getExpression();
    }
    return ObjectUtil.tryCast(e, GoReferenceExpression.class);
  }

  @Contract("null -> false")
  private static boolean isFieldReferenceExpression(@Nullable PsiElement element) {
    return element instanceof GoReferenceExpression && isFieldDefinition(((GoReferenceExpression)element).resolve());
  }

  @Contract("null -> false")
  private static boolean isFieldDefinition(@Nullable PsiElement element) {
    return element instanceof GoFieldDefinition || element instanceof GoAnonymousFieldDefinition;
  }

  private static boolean isAssignedInPreviousStatement(@Nonnull GoExpression referenceExpression,
                                                       @Nonnull GoAssignmentStatement assignment) {
    GoReferenceExpression rightExpression =
      unwrapParensAndCast(GoPsiImplUtil.getRightExpression(assignment, getTopmostExpression(referenceExpression)));

    PsiElement resolve = rightExpression != null ? rightExpression.resolve() : null;
    GoStatement previousElement = resolve != null ? PsiTreeUtil.getPrevSiblingOfType(assignment, GoStatement.class) : null;
    return previousElement != null && exists(getLeftHandElements(previousElement), e -> isResolvedTo(e, resolve));
  }

  @Nonnull
  private static GoExpression getTopmostExpression(@Nonnull GoExpression expression) {
    return ObjectUtil.notNull(PsiTreeUtil.getTopmostParentOfType(expression, GoExpression.class), expression);
  }

  private static boolean isResolvedTo(@Nullable PsiElement e, @Nullable PsiElement resolve) {
    if (e instanceof GoVarDefinition) return resolve == e;

    GoReferenceExpression refExpression = unwrapParensAndCast(e);
    return refExpression != null && refExpression.resolve() == resolve;
  }

  @Nonnull
  private static List<GoReferenceExpression> getUninitializedSingleFieldReferences(@Nonnull GoAssignmentStatement assignment,
                                                                                   @Nonnull GoReferenceExpression fieldReferenceExpression,
                                                                                   @Nonnull GoCompositeLit compositeLit) {
    PsiElement resolve = resolveQualifier(fieldReferenceExpression);
    List<GoReferenceExpression> uninitializedFieldReferencesByQualifier =
      filter(getUninitializedFieldReferenceExpressions(assignment, compositeLit), e -> isResolvedTo(e.getQualifier(), resolve));
    MultiMap<PsiElement, GoReferenceExpression> resolved = groupBy(uninitializedFieldReferencesByQualifier, GoReferenceExpression::resolve);
    return map(filter(resolved.entrySet(), set -> set.getValue().size() == 1), set -> getFirstItem(set.getValue()));
  }

  @Nullable
  private static GoCompositeLit getStructLiteralByReference(@Nonnull GoReferenceExpression fieldReferenceExpression,
                                                            @Nonnull GoAssignmentStatement assignment) {
    GoStatement previousStatement = PsiTreeUtil.getPrevSiblingOfType(assignment, GoStatement.class);
    if (previousStatement instanceof GoSimpleStatement) {
      return getStructLiteral(fieldReferenceExpression, (GoSimpleStatement)previousStatement);
    }
    if (previousStatement instanceof GoAssignmentStatement) {
      return getStructLiteral(fieldReferenceExpression, (GoAssignmentStatement)previousStatement);
    }
    return null;
  }

  @Nullable
  private static GoCompositeLit getStructLiteral(@Nonnull GoReferenceExpression fieldReferenceExpression,
                                                 @Nonnull GoSimpleStatement structDeclaration) {
    GoShortVarDeclaration varDeclaration = structDeclaration.getShortVarDeclaration();
    if (varDeclaration == null) return null;

    PsiElement resolve = resolveQualifier(fieldReferenceExpression);
    GoVarDefinition structVarDefinition = find(varDeclaration.getVarDefinitionList(), definition -> resolve == definition);
    return structVarDefinition != null ? ObjectUtil.tryCast(structVarDefinition.getValue(), GoCompositeLit.class) : null;
  }

  @Nullable
  private static PsiElement resolveQualifier(@Nonnull GoReferenceExpression fieldReferenceExpression) {
    GoReferenceExpression qualifier = fieldReferenceExpression.getQualifier();
    return qualifier != null ? qualifier.resolve() : null;
  }

  @Nullable
  private static GoCompositeLit getStructLiteral(@Nonnull GoReferenceExpression fieldReferenceExpression,
                                                 @Nonnull GoAssignmentStatement structAssignment) {
    GoVarDefinition varDefinition = ObjectUtil.tryCast(resolveQualifier(fieldReferenceExpression), GoVarDefinition.class);
    PsiElement field = fieldReferenceExpression.resolve();
    if (varDefinition == null || !isFieldDefinition(field) || !hasStructTypeWithField(varDefinition, (GoNamedElement)field)) {
      return null;
    }

    GoExpression structReferenceExpression = find(structAssignment.getLeftHandExprList().getExpressionList(),
                                                  expression -> isResolvedTo(expression, varDefinition));
    if (structReferenceExpression == null) return null;
    GoExpression compositeLit = GoPsiImplUtil.getRightExpression(structAssignment, structReferenceExpression);
    return ObjectUtil.tryCast(compositeLit, GoCompositeLit.class);
  }

  private static boolean hasStructTypeWithField(@Nonnull GoVarDefinition structVarDefinition, @Nonnull GoNamedElement field) {
    GoType type = structVarDefinition.getGoType(null);
    GoStructType structType = type != null ? ObjectUtil.tryCast(type.getUnderlyingType(), GoStructType.class) : null;
    return structType != null && PsiTreeUtil.isAncestor(structType, field, true);
  }

  private static boolean isFieldInitialization(@Nonnull GoElement element, @Nonnull PsiElement field) {
    GoKey key = element.getKey();
    GoFieldName fieldName = key != null ? key.getFieldName() : null;
    return fieldName != null && fieldName.resolve() == field;
  }

  @Nonnull
  private static List<GoReferenceExpression> getUninitializedFieldReferenceExpressions(@Nonnull GoAssignmentStatement assignment,
                                                                                       @Nonnull GoCompositeLit structLiteral) {
    return filter(getFieldReferenceExpressions(assignment), expression ->
      isUninitializedFieldReferenceExpression(expression, structLiteral) && !isAssignedInPreviousStatement(expression, assignment));
  }

  @Contract("null, _-> false")
  private static boolean isUninitializedFieldReferenceExpression(@Nullable GoReferenceExpression fieldReferenceExpression,
                                                                 @Nonnull GoCompositeLit structLiteral) {
    if (fieldReferenceExpression == null) return false;
    GoLiteralValue literalValue = structLiteral.getLiteralValue();
    PsiElement resolve = fieldReferenceExpression.resolve();
    return literalValue != null && isFieldDefinition(resolve) &&
           !exists(literalValue.getElementList(), element -> isFieldInitialization(element, resolve));
  }

  @Nonnull
  private static List<? extends PsiElement> getLeftHandElements(@Nonnull GoStatement statement) {
    if (statement instanceof GoSimpleStatement) {
      GoShortVarDeclaration varDeclaration = ((GoSimpleStatement)statement).getShortVarDeclaration();
      return varDeclaration != null ? varDeclaration.getVarDefinitionList() : List.of();
    }
    if (statement instanceof GoAssignmentStatement) {
      return ((GoAssignmentStatement)statement).getLeftHandExprList().getExpressionList();
    }
    return List.of();
  }

  @Override
  public void invoke(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) throws IncorrectOperationException {
    Data data = getData(element);
    if (data == null) return;
    moveFieldReferenceExpressions(data);
  }

  private static void moveFieldReferenceExpressions(@Nonnull Data data) {
    GoLiteralValue literalValue = data.getCompositeLit().getLiteralValue();
    if (literalValue == null) return;

    for (GoReferenceExpression expression : data.getReferenceExpressions()) {
      GoExpression anchor = getTopmostExpression(expression);
      GoExpression fieldValue = GoPsiImplUtil.getRightExpression(data.getAssignment(), anchor);
      if (fieldValue == null) continue;

      GoPsiImplUtil.deleteExpressionFromAssignment(data.getAssignment(), anchor);
      addFieldDefinition(literalValue, expression.getIdentifier().getText(), fieldValue.getText());
    }
  }

  private static void addFieldDefinition(@Nonnull GoLiteralValue literalValue, @Nonnull String name, @Nonnull String value) {
    Project project = literalValue.getProject();
    PsiElement newField = GoElementFactory.createLiteralValueElement(project, name, value);
    GoElement lastElement = getLastItem(literalValue.getElementList());
    if (lastElement == null) {
      literalValue.addAfter(newField, literalValue.getLbrace());
    }
    else {
      lastElement.add(GoElementFactory.createComma(project));
      lastElement.add(newField);
    }
  }

  private static class Data {
    private final GoCompositeLit myCompositeLit;
    private final GoAssignmentStatement myAssignment;
    private final List<GoReferenceExpression> myReferenceExpressions;

    public Data(@Nonnull GoAssignmentStatement assignment,
                @Nonnull GoCompositeLit compositeLit,
                @Nonnull List<GoReferenceExpression> referenceExpressions) {
      myCompositeLit = compositeLit;
      myAssignment = assignment;
      myReferenceExpressions = referenceExpressions;
    }

    public GoCompositeLit getCompositeLit() {
      return myCompositeLit;
    }

    public GoAssignmentStatement getAssignment() {
      return myAssignment;
    }

    public List<GoReferenceExpression> getReferenceExpressions() {
      return myReferenceExpressions;
    }
  }
}
