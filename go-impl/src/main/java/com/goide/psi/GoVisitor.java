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

// This is a generated file. Not intended for manual editing.
package com.goide.psi;

import javax.annotation.Nonnull;

import com.intellij.psi.PsiElementVisitor;

public class GoVisitor extends PsiElementVisitor {

  public void visitAddExpr(@Nonnull GoAddExpr o) {
    visitBinaryExpr(o);
  }

  public void visitAndExpr(@Nonnull GoAndExpr o) {
    visitBinaryExpr(o);
  }

  public void visitAnonymousFieldDefinition(@Nonnull GoAnonymousFieldDefinition o) {
    visitNamedElement(o);
  }

  public void visitArgumentList(@Nonnull GoArgumentList o) {
    visitCompositeElement(o);
  }

  public void visitArrayOrSliceType(@Nonnull GoArrayOrSliceType o) {
    visitType(o);
  }

  public void visitAssignmentStatement(@Nonnull GoAssignmentStatement o) {
    visitStatement(o);
  }

  public void visitBinaryExpr(@Nonnull GoBinaryExpr o) {
    visitExpression(o);
  }

  public void visitBlock(@Nonnull GoBlock o) {
    visitCompositeElement(o);
  }

  public void visitBreakStatement(@Nonnull GoBreakStatement o) {
    visitStatement(o);
  }

  public void visitBuiltinArgumentList(@Nonnull GoBuiltinArgumentList o) {
    visitArgumentList(o);
  }

  public void visitBuiltinCallExpr(@Nonnull GoBuiltinCallExpr o) {
    visitExpression(o);
  }

  public void visitCallExpr(@Nonnull GoCallExpr o) {
    visitExpression(o);
  }

  public void visitChannelType(@Nonnull GoChannelType o) {
    visitType(o);
  }

  public void visitCommCase(@Nonnull GoCommCase o) {
    visitCompositeElement(o);
  }

  public void visitCommClause(@Nonnull GoCommClause o) {
    visitCompositeElement(o);
  }

  public void visitCompositeLit(@Nonnull GoCompositeLit o) {
    visitExpression(o);
  }

  public void visitConditionalExpr(@Nonnull GoConditionalExpr o) {
    visitBinaryExpr(o);
  }

  public void visitConstDeclaration(@Nonnull GoConstDeclaration o) {
    visitTopLevelDeclaration(o);
  }

  public void visitConstDefinition(@Nonnull GoConstDefinition o) {
    visitNamedElement(o);
  }

  public void visitConstSpec(@Nonnull GoConstSpec o) {
    visitCompositeElement(o);
  }

  public void visitContinueStatement(@Nonnull GoContinueStatement o) {
    visitStatement(o);
  }

  public void visitConversionExpr(@Nonnull GoConversionExpr o) {
    visitBinaryExpr(o);
  }

  public void visitDeferStatement(@Nonnull GoDeferStatement o) {
    visitStatement(o);
  }

  public void visitElement(@Nonnull GoElement o) {
    visitCompositeElement(o);
  }

  public void visitElseStatement(@Nonnull GoElseStatement o) {
    visitStatement(o);
  }

  public void visitExprCaseClause(@Nonnull GoExprCaseClause o) {
    visitCaseClause(o);
  }

  public void visitExprSwitchStatement(@Nonnull GoExprSwitchStatement o) {
    visitSwitchStatement(o);
  }

  public void visitExpression(@Nonnull GoExpression o) {
    visitTypeOwner(o);
  }

  public void visitFallthroughStatement(@Nonnull GoFallthroughStatement o) {
    visitStatement(o);
  }

  public void visitFieldDeclaration(@Nonnull GoFieldDeclaration o) {
    visitCompositeElement(o);
  }

  public void visitFieldDefinition(@Nonnull GoFieldDefinition o) {
    visitNamedElement(o);
  }

  public void visitFieldName(@Nonnull GoFieldName o) {
    visitReferenceExpressionBase(o);
  }

  public void visitForClause(@Nonnull GoForClause o) {
    visitCompositeElement(o);
  }

  public void visitForStatement(@Nonnull GoForStatement o) {
    visitStatement(o);
  }

  public void visitFunctionDeclaration(@Nonnull GoFunctionDeclaration o) {
    visitFunctionOrMethodDeclaration(o);
  }

  public void visitFunctionLit(@Nonnull GoFunctionLit o) {
    visitExpression(o);
    // visitSignatureOwner(o);
  }

  public void visitFunctionType(@Nonnull GoFunctionType o) {
    visitType(o);
    // visitSignatureOwner(o);
  }

  public void visitGoStatement(@Nonnull GoGoStatement o) {
    visitStatement(o);
  }

  public void visitGotoStatement(@Nonnull GoGotoStatement o) {
    visitStatement(o);
  }

  public void visitIfStatement(@Nonnull GoIfStatement o) {
    visitStatement(o);
  }

  public void visitImportDeclaration(@Nonnull GoImportDeclaration o) {
    visitCompositeElement(o);
  }

  public void visitImportList(@Nonnull GoImportList o) {
    visitCompositeElement(o);
  }

  public void visitImportSpec(@Nonnull GoImportSpec o) {
    visitNamedElement(o);
  }

  public void visitImportString(@Nonnull GoImportString o) {
    visitCompositeElement(o);
  }

  public void visitIncDecStatement(@Nonnull GoIncDecStatement o) {
    visitStatement(o);
  }

  public void visitIndexOrSliceExpr(@Nonnull GoIndexOrSliceExpr o) {
    visitExpression(o);
  }

  public void visitInterfaceType(@Nonnull GoInterfaceType o) {
    visitType(o);
  }

  public void visitKey(@Nonnull GoKey o) {
    visitCompositeElement(o);
  }

  public void visitLabelDefinition(@Nonnull GoLabelDefinition o) {
    visitNamedElement(o);
  }

  public void visitLabelRef(@Nonnull GoLabelRef o) {
    visitCompositeElement(o);
  }

  public void visitLabeledStatement(@Nonnull GoLabeledStatement o) {
    visitStatement(o);
  }

  public void visitLeftHandExprList(@Nonnull GoLeftHandExprList o) {
    visitCompositeElement(o);
  }

  public void visitLiteral(@Nonnull GoLiteral o) {
    visitExpression(o);
  }

  public void visitLiteralTypeExpr(@Nonnull GoLiteralTypeExpr o) {
    visitExpression(o);
  }

  public void visitLiteralValue(@Nonnull GoLiteralValue o) {
    visitCompositeElement(o);
  }

  public void visitMapType(@Nonnull GoMapType o) {
    visitType(o);
  }

  public void visitMethodDeclaration(@Nonnull GoMethodDeclaration o) {
    visitFunctionOrMethodDeclaration(o);
  }

  public void visitMethodSpec(@Nonnull GoMethodSpec o) {
    visitNamedSignatureOwner(o);
  }

  public void visitMulExpr(@Nonnull GoMulExpr o) {
    visitBinaryExpr(o);
  }

  public void visitOrExpr(@Nonnull GoOrExpr o) {
    visitBinaryExpr(o);
  }

  public void visitPackageClause(@Nonnull GoPackageClause o) {
    visitCompositeElement(o);
  }

  public void visitParType(@Nonnull GoParType o) {
    visitType(o);
  }

  public void visitParamDefinition(@Nonnull GoParamDefinition o) {
    visitNamedElement(o);
  }

  public void visitParameterDeclaration(@Nonnull GoParameterDeclaration o) {
    visitCompositeElement(o);
  }

  public void visitParameters(@Nonnull GoParameters o) {
    visitCompositeElement(o);
  }

  public void visitParenthesesExpr(@Nonnull GoParenthesesExpr o) {
    visitExpression(o);
  }

  public void visitPointerType(@Nonnull GoPointerType o) {
    visitType(o);
  }

  public void visitRangeClause(@Nonnull GoRangeClause o) {
    visitVarSpec(o);
  }

  public void visitReceiver(@Nonnull GoReceiver o) {
    visitNamedElement(o);
  }

  public void visitRecvStatement(@Nonnull GoRecvStatement o) {
    visitVarSpec(o);
  }

  public void visitReferenceExpression(@Nonnull GoReferenceExpression o) {
    visitExpression(o);
    // visitReferenceExpressionBase(o);
  }

  public void visitResult(@Nonnull GoResult o) {
    visitCompositeElement(o);
  }

  public void visitReturnStatement(@Nonnull GoReturnStatement o) {
    visitStatement(o);
  }

  public void visitSelectStatement(@Nonnull GoSelectStatement o) {
    visitStatement(o);
  }

  public void visitSelectorExpr(@Nonnull GoSelectorExpr o) {
    visitBinaryExpr(o);
  }

  public void visitSendStatement(@Nonnull GoSendStatement o) {
    visitStatement(o);
  }

  public void visitShortVarDeclaration(@Nonnull GoShortVarDeclaration o) {
    visitVarSpec(o);
  }

  public void visitSignature(@Nonnull GoSignature o) {
    visitCompositeElement(o);
  }

  public void visitSimpleStatement(@Nonnull GoSimpleStatement o) {
    visitStatement(o);
  }

  public void visitSpecType(@Nonnull GoSpecType o) {
    visitType(o);
  }

  public void visitStatement(@Nonnull GoStatement o) {
    visitCompositeElement(o);
  }

  public void visitStringLiteral(@Nonnull GoStringLiteral o) {
    visitExpression(o);
    // visitPsiLanguageInjectionHost(o);
  }

  public void visitStructType(@Nonnull GoStructType o) {
    visitType(o);
  }

  public void visitSwitchStart(@Nonnull GoSwitchStart o) {
    visitCompositeElement(o);
  }

  public void visitSwitchStatement(@Nonnull GoSwitchStatement o) {
    visitStatement(o);
  }

  public void visitTag(@Nonnull GoTag o) {
    visitCompositeElement(o);
  }

  public void visitType(@Nonnull GoType o) {
    visitCompositeElement(o);
  }

  public void visitTypeAssertionExpr(@Nonnull GoTypeAssertionExpr o) {
    visitExpression(o);
  }

  public void visitTypeCaseClause(@Nonnull GoTypeCaseClause o) {
    visitCaseClause(o);
  }

  public void visitTypeDeclaration(@Nonnull GoTypeDeclaration o) {
    visitTopLevelDeclaration(o);
  }

  public void visitTypeGuard(@Nonnull GoTypeGuard o) {
    visitCompositeElement(o);
  }

  public void visitTypeList(@Nonnull GoTypeList o) {
    visitType(o);
  }

  public void visitTypeReferenceExpression(@Nonnull GoTypeReferenceExpression o) {
    visitReferenceExpressionBase(o);
  }

  public void visitTypeSpec(@Nonnull GoTypeSpec o) {
    visitNamedElement(o);
  }

  public void visitTypeSwitchGuard(@Nonnull GoTypeSwitchGuard o) {
    visitCompositeElement(o);
  }

  public void visitTypeSwitchStatement(@Nonnull GoTypeSwitchStatement o) {
    visitSwitchStatement(o);
  }

  public void visitUnaryExpr(@Nonnull GoUnaryExpr o) {
    visitExpression(o);
  }

  public void visitValue(@Nonnull GoValue o) {
    visitCompositeElement(o);
  }

  public void visitVarDeclaration(@Nonnull GoVarDeclaration o) {
    visitTopLevelDeclaration(o);
  }

  public void visitVarDefinition(@Nonnull GoVarDefinition o) {
    visitNamedElement(o);
  }

  public void visitVarSpec(@Nonnull GoVarSpec o) {
    visitCompositeElement(o);
  }

  public void visitAssignOp(@Nonnull GoAssignOp o) {
    visitCompositeElement(o);
  }

  public void visitCaseClause(@Nonnull GoCaseClause o) {
    visitCompositeElement(o);
  }

  public void visitFunctionOrMethodDeclaration(@Nonnull GoFunctionOrMethodDeclaration o) {
    visitCompositeElement(o);
  }

  public void visitNamedElement(@Nonnull GoNamedElement o) {
    visitCompositeElement(o);
  }

  public void visitNamedSignatureOwner(@Nonnull GoNamedSignatureOwner o) {
    visitCompositeElement(o);
  }

  public void visitReferenceExpressionBase(@Nonnull GoReferenceExpressionBase o) {
    visitCompositeElement(o);
  }

  public void visitTopLevelDeclaration(@Nonnull GoTopLevelDeclaration o) {
    visitCompositeElement(o);
  }

  public void visitTypeOwner(@Nonnull GoTypeOwner o) {
    visitCompositeElement(o);
  }

  public void visitCompositeElement(@Nonnull GoCompositeElement o) {
    visitElement(o);
  }

}
