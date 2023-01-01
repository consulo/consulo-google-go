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

package com.goide.psi.impl;

import com.goide.GoLanguage;
import com.goide.psi.*;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.PsiParserFacade;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("ConstantConditions")
public class GoElementFactory {
  private GoElementFactory() {
  }

  @Nonnull
  private static GoFile createFileFromText(@Nonnull Project project, @Nonnull String text) {
    return (GoFile)PsiFileFactory.getInstance(project).createFileFromText("a.go", GoLanguage.INSTANCE, text);
  }

  @Nonnull
  public static PsiElement createReturnStatement(@Nonnull Project project) {
    GoFile file = createFileFromText(project, "package main\nfunc _() { return; }");
    return PsiTreeUtil.findChildOfType(file, GoReturnStatement.class);
  }

  @Nonnull
  public static PsiElement createSelectStatement(@Nonnull Project project) {
    GoFile file = createFileFromText(project, "package main\nfunc _() { select {\n} }");
    return PsiTreeUtil.findChildOfType(file, GoSelectStatement.class);
  }

  @Nonnull
  public static PsiElement createIdentifierFromText(@Nonnull Project project, String text) {
    GoFile file = createFileFromText(project, "package " + text);
    return file.getPackage().getIdentifier();
  }

  @Nonnull
  public static GoIfStatement createIfStatement(@Nonnull Project project,
                                                @Nonnull String condition,
                                                @Nonnull String thenBranch,
                                                @Nullable String elseBranch) {
    String elseText = elseBranch != null ? " else {\n" + elseBranch + "\n}" : "";
    GoFile file = createFileFromText(project, "package a; func _() {\n" +
                                              "if " + condition + " {\n" +
                                              thenBranch + "\n" +
                                              "}" + elseText + "\n" +
                                              "}");
    return PsiTreeUtil.findChildOfType(file, GoIfStatement.class);
  }

  @Nonnull
  public static GoImportDeclaration createEmptyImportDeclaration(@Nonnull Project project) {
    return PsiTreeUtil.findChildOfType(createFileFromText(project, "package main\nimport (\n\n)"), GoImportDeclaration.class);
  }
                                                            
  @Nonnull
  public static GoImportDeclaration createImportDeclaration(@Nonnull Project project, @Nonnull String importString,
                                                            @Nullable String alias, boolean withParens) {
    importString = GoPsiImplUtil.isQuotedImportString(importString) ? importString : StringUtil.wrapWithDoubleQuote(importString);
    alias = alias != null ? alias + " " : "";
    GoFile file = createFileFromText(project, withParens
                                              ? "package main\nimport (\n" + alias + importString + "\n)"
                                              : "package main\nimport " + alias + importString);
    return PsiTreeUtil.findChildOfType(file, GoImportDeclaration.class);
  }

  @Nonnull
  public static GoImportSpec createImportSpec(@Nonnull Project project, @Nonnull String importString, @Nullable String alias) {
    GoImportDeclaration importDeclaration = createImportDeclaration(project, importString, alias, true);
    return ContainerUtil.getFirstItem(importDeclaration.getImportSpecList());
  }

  @Nonnull
  public static GoImportString createImportString(@Nonnull Project project, @Nonnull String importString) {
    GoImportSpec importSpec = createImportSpec(project, importString, null);
    return importSpec.getImportString();
  }

  @Nonnull
  public static PsiElement createNewLine(@Nonnull Project project) {
    return PsiParserFacade.getInstance(project).createWhiteSpaceFromText("\n");
  }

  @Nonnull
  public static PsiElement createComma(@Nonnull Project project) {
    return createFileFromText(project, "package foo; var a,b = 1,2").getVars().get(0).getNextSibling();
  }

  @Nonnull
  public static GoPackageClause createPackageClause(@Nonnull Project project, @Nonnull String name) {
    return createFileFromText(project, "package " + name).getPackage();
  }

  @Nonnull
  public static GoBlock createBlock(@Nonnull Project project) {
    GoFunctionDeclaration function = ContainerUtil.getFirstItem(createFileFromText(project, "package a; func t() {\n}").getFunctions());
    assert function != null : "Impossible situation! Parser is broken.";
    return function.getBlock();
  }

  @Nonnull
  public static GoStringLiteral createStringLiteral(@Nonnull Project project, @Nonnull String stringLiteral) {
    GoFile f = createFileFromText(project, "package a; var b = " + stringLiteral);
    return PsiTreeUtil.getNextSiblingOfType(ContainerUtil.getFirstItem(f.getVars()), GoStringLiteral.class);
  }

  @Nonnull
  public static GoSignature createFunctionSignatureFromText(@Nonnull Project project, @Nonnull String text) {
    GoFile file = createFileFromText(project, "package a; func t(" + text + ") {\n}");
    return ContainerUtil.getFirstItem(file.getFunctions()).getSignature();
  }

  @Nonnull
  public static GoStatement createShortVarDeclarationStatement(@Nonnull Project project,
                                                               @Nonnull String name,
                                                               @Nonnull GoExpression initializer) {
    GoFile file = createFileFromText(project, "package a; func a() {\n " + name + " := " + initializer.getText() + "}");
    return PsiTreeUtil.findChildOfType(file, GoSimpleStatement.class);
  }

  @Nonnull
  public static GoStatement createShortVarDeclarationStatement(@Nonnull Project project,
                                                               @Nonnull String leftSide,
                                                               @Nonnull String rightSide) {
    GoFile file = createFileFromText(project, "package a; func a() {\n " + leftSide + " := " + rightSide + "}");
    return PsiTreeUtil.findChildOfType(file, GoSimpleStatement.class);
  }

  @Nonnull
  public static GoRangeClause createRangeClause(@Nonnull Project project, @Nonnull String leftSide, @Nonnull String rightSide) {
    GoFile file = createFileFromText(project, "package a; func a() {\n for " + leftSide + " := range " + rightSide + "{\n}\n}");
    return PsiTreeUtil.findChildOfType(file, GoRangeClause.class);
  }

  @Nonnull
  public static GoRangeClause createRangeClauseAssignment(@Nonnull Project project, @Nonnull String leftSide, @Nonnull String rightSide) {
    GoFile file = createFileFromText(project, "package a; func a() {\n for " + leftSide + " = range " + rightSide + "{\n}\n}");
    return PsiTreeUtil.findChildOfType(file, GoRangeClause.class);
  }

  @Nonnull
  public static GoRecvStatement createRecvStatement(@Nonnull Project project, @Nonnull String leftSide, @Nonnull String rightSide) {
    GoFile file = createFileFromText(project, "package a; func a() {\n select { case " + leftSide + " := " + rightSide + ":\n}\n}");
    return PsiTreeUtil.findChildOfType(file, GoRecvStatement.class);
  }

  @Nonnull
  public static GoRecvStatement createRecvStatementAssignment(@Nonnull Project project, @Nonnull String left, @Nonnull String right) {
    GoFile file = createFileFromText(project, "package a; func a() {\n select { case " + left + " = " + right + ":\n}\n}");
    return PsiTreeUtil.findChildOfType(file, GoRecvStatement.class);
  }

  public static GoAssignmentStatement createAssignmentStatement(@Nonnull Project project, @Nonnull String left, @Nonnull String right) {
    GoFile file = createFileFromText(project, "package a; func a() {\n " + left + " = " + right + "}");
    return PsiTreeUtil.findChildOfType(file, GoAssignmentStatement.class);
  }

  @Nonnull
  public static GoDeferStatement createDeferStatement(@Nonnull Project project, @Nonnull String expressionText) {
    GoFile file = createFileFromText(project, "package a; func a() {\n  defer " + expressionText + "}");
    return PsiTreeUtil.findChildOfType(file, GoDeferStatement.class);
  }

  @Nonnull
  public static GoGoStatement createGoStatement(@Nonnull Project project, @Nonnull String expressionText) {
    GoFile file = createFileFromText(project, "package a; func a() {\n  go " + expressionText + "}");
    return PsiTreeUtil.findChildOfType(file, GoGoStatement.class);
  }

  @Nonnull
  public static GoForStatement createForStatement(@Nonnull Project project, @Nonnull String text) {
    GoFile file = createFileFromText(project, "package a; func a() {\n for {\n" + text +  "\n}\n}");
    return PsiTreeUtil.findChildOfType(file, GoForStatement.class);
  }

  @Nonnull
  public static GoExpression createExpression(@Nonnull Project project, @Nonnull String text) {
    GoFile file = createFileFromText(project, "package a; func a() {\n a := " + text + "}");
    return PsiTreeUtil.findChildOfType(file, GoExpression.class);
  }

  @Nonnull
  public static GoReferenceExpression createReferenceExpression(@Nonnull Project project, @Nonnull String name) {
    GoFile file = createFileFromText(project, "package a; var a = " + name);
    return PsiTreeUtil.findChildOfType(file, GoReferenceExpression.class);
  }

  @Nonnull
  public static GoSimpleStatement createComparison(@Nonnull Project project, @Nonnull String text) {
    GoFile file = createFileFromText(project, "package a; func a() {\n " + text + "}");
    return PsiTreeUtil.findChildOfType(file, GoSimpleStatement.class);
  }

  @Nonnull
  public static GoConstDeclaration createConstDeclaration(@Nonnull Project project, @Nonnull String text) {
    GoFile file = createFileFromText(project, "package a; const " + text);
    return PsiTreeUtil.findChildOfType(file, GoConstDeclaration.class);
  }

  @Nonnull
  public static GoConstSpec createConstSpec(@Nonnull Project project, @Nonnull String name, @Nullable String type, @Nullable String value) {
    GoConstDeclaration constDeclaration = createConstDeclaration(project, prepareVarOrConstDeclarationText(name, type, value));
    return ContainerUtil.getFirstItem(constDeclaration.getConstSpecList());
  }

  @Nonnull
  public static GoVarDeclaration createVarDeclaration(@Nonnull Project project, @Nonnull String text) {
    GoFile file = createFileFromText(project, "package a; var " + text);
    return PsiTreeUtil.findChildOfType(file, GoVarDeclaration.class);
  }

  @Nonnull
  public static GoVarSpec createVarSpec(@Nonnull Project project, @Nonnull String name, @Nullable String type, @Nullable String value) {
    GoVarDeclaration varDeclaration = createVarDeclaration(project, prepareVarOrConstDeclarationText(name, type, value));
    return ContainerUtil.getFirstItem(varDeclaration.getVarSpecList());
  }

  @Nonnull
  private static String prepareVarOrConstDeclarationText(@Nonnull String name, @Nullable String type, @Nullable String value) {
    type = StringUtil.trim(type);
    value = StringUtil.trim(value);
    type = StringUtil.isNotEmpty(type) ? " " + type : "";
    value = StringUtil.isNotEmpty(value) ? " = " + value : "";
    return name + type + value;
  }

  public static GoTypeList createTypeList(@Nonnull Project project, @Nonnull String text) {
    GoFile file = createFileFromText(project, "package a; func _() (" + text + "){}");
    return PsiTreeUtil.findChildOfType(file, GoTypeList.class);
  }

  public static GoType createType(@Nonnull Project project, @Nonnull String text) {
    GoFile file = createFileFromText(project, "package a; var a " + text);
    return PsiTreeUtil.findChildOfType(file, GoType.class);
  }

  public static PsiElement createLiteralValueElement(@Nonnull Project project, @Nonnull String key, @Nonnull String value) {
    GoFile file = createFileFromText(project, "package a; var _ = struct { a string } { " + key + ": " + value + " }");
    return PsiTreeUtil.findChildOfType(file, GoElement.class);
  }

  @Nonnull
  public static GoTypeDeclaration createTypeDeclaration(@Nonnull Project project, @Nonnull String name, @Nonnull GoType type) {
    GoFile file = createFileFromText(project, "package a; type " + name + " " + type.getText());
    return PsiTreeUtil.findChildOfType(file, GoTypeDeclaration.class);
  }
}