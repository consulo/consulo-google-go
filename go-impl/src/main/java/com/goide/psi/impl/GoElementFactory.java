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

import org.jspecify.annotations.Nullable;

@SuppressWarnings("ConstantConditions")
public class GoElementFactory {
  private GoElementFactory() {
  }

  private static GoFile createFileFromText(Project project, String text) {
    return (GoFile)PsiFileFactory.getInstance(project).createFileFromText("a.go", GoLanguage.INSTANCE, text);
  }

  public static PsiElement createReturnStatement(Project project) {
    GoFile file = createFileFromText(project, "package main\nfunc _() { return; }");
    return PsiTreeUtil.findChildOfType(file, GoReturnStatement.class);
  }

  public static PsiElement createSelectStatement(Project project) {
    GoFile file = createFileFromText(project, "package main\nfunc _() { select {\n} }");
    return PsiTreeUtil.findChildOfType(file, GoSelectStatement.class);
  }

  public static PsiElement createIdentifierFromText(Project project, String text) {
    GoFile file = createFileFromText(project, "package " + text);
    return file.getPackage().getIdentifier();
  }

  public static GoIfStatement createIfStatement(Project project,
                                                String condition,
                                                String thenBranch,
                                                @Nullable String elseBranch) {
    String elseText = elseBranch != null ? " else {\n" + elseBranch + "\n}" : "";
    GoFile file = createFileFromText(project, "package a; func _() {\n" +
                                              "if " + condition + " {\n" +
                                              thenBranch + "\n" +
                                              "}" + elseText + "\n" +
                                              "}");
    return PsiTreeUtil.findChildOfType(file, GoIfStatement.class);
  }

  public static GoImportDeclaration createEmptyImportDeclaration(Project project) {
    return PsiTreeUtil.findChildOfType(createFileFromText(project, "package main\nimport (\n\n)"), GoImportDeclaration.class);
  }
                                                            
  public static GoImportDeclaration createImportDeclaration(Project project, String importString,
                                                            @Nullable String alias, boolean withParens) {
    importString = GoPsiImplUtil.isQuotedImportString(importString) ? importString : StringUtil.wrapWithDoubleQuote(importString);
    alias = alias != null ? alias + " " : "";
    GoFile file = createFileFromText(project, withParens
                                              ? "package main\nimport (\n" + alias + importString + "\n)"
                                              : "package main\nimport " + alias + importString);
    return PsiTreeUtil.findChildOfType(file, GoImportDeclaration.class);
  }

  public static GoImportSpec createImportSpec(Project project, String importString, @Nullable String alias) {
    GoImportDeclaration importDeclaration = createImportDeclaration(project, importString, alias, true);
    return ContainerUtil.getFirstItem(importDeclaration.getImportSpecList());
  }

  public static GoImportString createImportString(Project project, String importString) {
    GoImportSpec importSpec = createImportSpec(project, importString, null);
    return importSpec.getImportString();
  }

  public static PsiElement createNewLine(Project project) {
    return PsiParserFacade.getInstance(project).createWhiteSpaceFromText("\n");
  }

  public static PsiElement createComma(Project project) {
    return createFileFromText(project, "package foo; var a,b = 1,2").getVars().get(0).getNextSibling();
  }

  public static GoPackageClause createPackageClause(Project project, String name) {
    return createFileFromText(project, "package " + name).getPackage();
  }

  public static GoBlock createBlock(Project project) {
    GoFunctionDeclaration function = ContainerUtil.getFirstItem(createFileFromText(project, "package a; func t() {\n}").getFunctions());
    assert function != null : "Impossible situation! Parser is broken.";
    return function.getBlock();
  }

  public static GoStringLiteral createStringLiteral(Project project, String stringLiteral) {
    GoFile f = createFileFromText(project, "package a; var b = " + stringLiteral);
    return PsiTreeUtil.getNextSiblingOfType(ContainerUtil.getFirstItem(f.getVars()), GoStringLiteral.class);
  }

  public static GoSignature createFunctionSignatureFromText(Project project, String text) {
    GoFile file = createFileFromText(project, "package a; func t(" + text + ") {\n}");
    return ContainerUtil.getFirstItem(file.getFunctions()).getSignature();
  }

  public static GoStatement createShortVarDeclarationStatement(Project project,
                                                               String name,
                                                               GoExpression initializer) {
    GoFile file = createFileFromText(project, "package a; func a() {\n " + name + " := " + initializer.getText() + "}");
    return PsiTreeUtil.findChildOfType(file, GoSimpleStatement.class);
  }

  public static GoStatement createShortVarDeclarationStatement(Project project,
                                                               String leftSide,
                                                               String rightSide) {
    GoFile file = createFileFromText(project, "package a; func a() {\n " + leftSide + " := " + rightSide + "}");
    return PsiTreeUtil.findChildOfType(file, GoSimpleStatement.class);
  }

  public static GoRangeClause createRangeClause(Project project, String leftSide, String rightSide) {
    GoFile file = createFileFromText(project, "package a; func a() {\n for " + leftSide + " := range " + rightSide + "{\n}\n}");
    return PsiTreeUtil.findChildOfType(file, GoRangeClause.class);
  }

  public static GoRangeClause createRangeClauseAssignment(Project project, String leftSide, String rightSide) {
    GoFile file = createFileFromText(project, "package a; func a() {\n for " + leftSide + " = range " + rightSide + "{\n}\n}");
    return PsiTreeUtil.findChildOfType(file, GoRangeClause.class);
  }

  public static GoRecvStatement createRecvStatement(Project project, String leftSide, String rightSide) {
    GoFile file = createFileFromText(project, "package a; func a() {\n select { case " + leftSide + " := " + rightSide + ":\n}\n}");
    return PsiTreeUtil.findChildOfType(file, GoRecvStatement.class);
  }

  public static GoRecvStatement createRecvStatementAssignment(Project project, String left, String right) {
    GoFile file = createFileFromText(project, "package a; func a() {\n select { case " + left + " = " + right + ":\n}\n}");
    return PsiTreeUtil.findChildOfType(file, GoRecvStatement.class);
  }

  public static GoAssignmentStatement createAssignmentStatement(Project project, String left, String right) {
    GoFile file = createFileFromText(project, "package a; func a() {\n " + left + " = " + right + "}");
    return PsiTreeUtil.findChildOfType(file, GoAssignmentStatement.class);
  }

  public static GoDeferStatement createDeferStatement(Project project, String expressionText) {
    GoFile file = createFileFromText(project, "package a; func a() {\n  defer " + expressionText + "}");
    return PsiTreeUtil.findChildOfType(file, GoDeferStatement.class);
  }

  public static GoGoStatement createGoStatement(Project project, String expressionText) {
    GoFile file = createFileFromText(project, "package a; func a() {\n  go " + expressionText + "}");
    return PsiTreeUtil.findChildOfType(file, GoGoStatement.class);
  }

  public static GoForStatement createForStatement(Project project, String text) {
    GoFile file = createFileFromText(project, "package a; func a() {\n for {\n" + text +  "\n}\n}");
    return PsiTreeUtil.findChildOfType(file, GoForStatement.class);
  }

  public static GoExpression createExpression(Project project, String text) {
    GoFile file = createFileFromText(project, "package a; func a() {\n a := " + text + "}");
    return PsiTreeUtil.findChildOfType(file, GoExpression.class);
  }

  public static GoReferenceExpression createReferenceExpression(Project project, String name) {
    GoFile file = createFileFromText(project, "package a; var a = " + name);
    return PsiTreeUtil.findChildOfType(file, GoReferenceExpression.class);
  }

  public static GoSimpleStatement createComparison(Project project, String text) {
    GoFile file = createFileFromText(project, "package a; func a() {\n " + text + "}");
    return PsiTreeUtil.findChildOfType(file, GoSimpleStatement.class);
  }

  public static GoConstDeclaration createConstDeclaration(Project project, String text) {
    GoFile file = createFileFromText(project, "package a; const " + text);
    return PsiTreeUtil.findChildOfType(file, GoConstDeclaration.class);
  }

  public static GoConstSpec createConstSpec(Project project, String name, @Nullable String type, @Nullable String value) {
    GoConstDeclaration constDeclaration = createConstDeclaration(project, prepareVarOrConstDeclarationText(name, type, value));
    return ContainerUtil.getFirstItem(constDeclaration.getConstSpecList());
  }

  public static GoVarDeclaration createVarDeclaration(Project project, String text) {
    GoFile file = createFileFromText(project, "package a; var " + text);
    return PsiTreeUtil.findChildOfType(file, GoVarDeclaration.class);
  }

  public static GoVarSpec createVarSpec(Project project, String name, @Nullable String type, @Nullable String value) {
    GoVarDeclaration varDeclaration = createVarDeclaration(project, prepareVarOrConstDeclarationText(name, type, value));
    return ContainerUtil.getFirstItem(varDeclaration.getVarSpecList());
  }

  private static String prepareVarOrConstDeclarationText(String name, @Nullable String type, @Nullable String value) {
    type = StringUtil.trim(type);
    value = StringUtil.trim(value);
    type = StringUtil.isNotEmpty(type) ? " " + type : "";
    value = StringUtil.isNotEmpty(value) ? " = " + value : "";
    return name + type + value;
  }

  public static GoTypeList createTypeList(Project project, String text) {
    GoFile file = createFileFromText(project, "package a; func _() (" + text + "){}");
    return PsiTreeUtil.findChildOfType(file, GoTypeList.class);
  }

  public static GoType createType(Project project, String text) {
    GoFile file = createFileFromText(project, "package a; var a " + text);
    return PsiTreeUtil.findChildOfType(file, GoType.class);
  }

  public static PsiElement createLiteralValueElement(Project project, String key, String value) {
    GoFile file = createFileFromText(project, "package a; var _ = struct { a string } { " + key + ": " + value + " }");
    return PsiTreeUtil.findChildOfType(file, GoElement.class);
  }

  public static GoTypeDeclaration createTypeDeclaration(Project project, String name, GoType type) {
    GoFile file = createFileFromText(project, "package a; type " + name + " " + type.getText());
    return PsiTreeUtil.findChildOfType(file, GoTypeDeclaration.class);
  }
}