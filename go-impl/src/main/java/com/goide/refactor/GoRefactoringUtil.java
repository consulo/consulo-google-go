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

package com.goide.refactor;

import com.goide.GoLanguage;
import com.goide.psi.*;
import com.goide.psi.impl.GoPsiImplUtil;
import com.goide.psi.impl.GoTypeUtil;
import consulo.application.util.matcher.NameUtil;
import consulo.component.util.text.UniqueNameGenerator;
import consulo.language.editor.PsiEquivalenceUtil;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.editor.refactoring.NamesValidator;
import consulo.language.editor.template.Expression;
import consulo.language.editor.template.ExpressionContext;
import consulo.language.editor.template.Result;
import consulo.language.editor.template.TextResult;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiRecursiveElementVisitor;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.util.collection.ArrayUtil;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.*;

public class GoRefactoringUtil {
  private GoRefactoringUtil() {
  }

  @Nonnull
  public static List<PsiElement> getLocalOccurrences(@Nonnull PsiElement element) {
    return getOccurrences(element, PsiTreeUtil.getTopmostParentOfType(element, GoBlock.class));
  }

  @Nonnull
  public static List<PsiElement> getOccurrences(@Nonnull PsiElement pattern, @Nullable PsiElement context) {
    if (context == null) return Collections.emptyList();
    List<PsiElement> occurrences = ContainerUtil.newArrayList();
    PsiRecursiveElementVisitor visitor = new PsiRecursiveElementVisitor() {
      @Override
      public void visitElement(@Nonnull PsiElement element) {
        if (PsiEquivalenceUtil.areElementsEquivalent(element, pattern)) {
          occurrences.add(element);
          return;
        }
        super.visitElement(element);
      }
    };
    context.acceptChildren(visitor);
    return occurrences;
  }

  @Nullable
  public static PsiElement findLocalAnchor(@Nonnull List<PsiElement> occurrences) {
    return findAnchor(occurrences, PsiTreeUtil.getNonStrictParentOfType(PsiTreeUtil.findCommonParent(occurrences), GoBlock.class));
  }

  @Nullable
  public static PsiElement findAnchor(@Nonnull List<PsiElement> occurrences, @Nullable PsiElement context) {
    PsiElement first = ContainerUtil.getFirstItem(occurrences);
    PsiElement statement = PsiTreeUtil.getNonStrictParentOfType(first, GoStatement.class);
    while (statement != null && statement.getParent() != context) {
      statement = statement.getParent();
    }
    return statement == null ? GoPsiImplUtil.getTopLevelDeclaration(first) : statement;
  }

  public static LinkedHashSet<String> getSuggestedNames(GoExpression expression) {
    return getSuggestedNames(expression, expression);
  }

  @Nonnull
  public static Expression createParameterNameSuggestedExpression(GoExpression expression) {
    GoTopLevelDeclaration topLevelDecl = PsiTreeUtil.getParentOfType(expression, GoTopLevelDeclaration.class);
    return new ParameterNameExpression(getSuggestedNames(expression, topLevelDecl != null ? topLevelDecl.getNextSibling() : null));
  }

  private static class ParameterNameExpression extends Expression {
    private final Set<String> myNames;

    public ParameterNameExpression(@Nonnull Set<String> names) {
      myNames = names;
    }

    @Nullable
    @Override
    public Result calculateResult(ExpressionContext context) {
      LookupElement firstElement = ArrayUtil.getFirstElement(calculateLookupItems(context));
      return new TextResult(firstElement != null ? firstElement.getLookupString() : "");
    }

    @Nullable
    @Override
    public Result calculateQuickResult(ExpressionContext context) {
      return null;
    }

    @Nonnull
    @Override
    public LookupElement[] calculateLookupItems(ExpressionContext context) {
      int offset = context.getStartOffset();
      Project project = context.getProject();
      PsiDocumentManager.getInstance(project).commitAllDocuments();
      assert context.getEditor() != null;
      PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(context.getEditor().getDocument());
      assert file != null;
      PsiElement elementAt = file.findElementAt(offset);

      Set<String> parameterNames = new HashSet<>();
      GoParameters parameters = PsiTreeUtil.getParentOfType(elementAt, GoParameters.class);
      if (parameters != null) {
        GoParamDefinition parameter = PsiTreeUtil.getParentOfType(elementAt, GoParamDefinition.class);
        for (GoParameterDeclaration paramDecl : parameters.getParameterDeclarationList()) {
          for (GoParamDefinition paramDef : paramDecl.getParamDefinitionList()) {
            if (parameter != paramDef) {
              parameterNames.add(paramDef.getName());
            }
          }
        }
      }

      Set<LookupElement> set = new LinkedHashSet<>();
      for (String name : myNames) {
        set.add(LookupElementBuilder.create(UniqueNameGenerator.generateUniqueName(name, parameterNames)));
      }
      return set.toArray(new LookupElement[set.size()]);
    }
  }

  @Nonnull
  private static LinkedHashSet<String> getSuggestedNames(GoExpression expression, PsiElement context) {
    // todo rewrite with names resolve; check occurrences contexts
    if (expression.isEquivalentTo(context)) {
      context = PsiTreeUtil.getParentOfType(context, GoBlock.class);
    }
    LinkedHashSet<String> usedNames = getNamesInContext(context);
    LinkedHashSet<String> names = new LinkedHashSet<>();
    NamesValidator namesValidator = NamesValidator.forLanguage(GoLanguage.INSTANCE);

    if (expression instanceof GoCallExpr) {
      GoReferenceExpression callReference = PsiTreeUtil.getChildOfType(expression, GoReferenceExpression.class);
      if (callReference != null) {
        String name = StringUtil.decapitalize(callReference.getIdentifier().getText());
        for (String candidate : NameUtil.getSuggestionsByName(name, "", "", false, false, false)) {
          if (usedNames.contains(candidate)) continue;
          if (!isValidName(namesValidator, candidate)) continue;
          names.add(candidate);
        }
      }
    }

    GoType type = expression.getGoType(null);
    String typeText = GoPsiImplUtil.getText(type);
    if (StringUtil.isNotEmpty(typeText)) {
      boolean array = GoTypeUtil.isIterable(type) && !GoTypeUtil.isString(type);
      for (String candidate : NameUtil.getSuggestionsByName(typeText, "", "", false, false, array)) {
        if (usedNames.contains(candidate) || typeText.equals(candidate)) continue;
        if (!isValidName(namesValidator, candidate)) continue;
        names.add(candidate);
      }
    }

    if (names.isEmpty()) {
      names.add(UniqueNameGenerator.generateUniqueName("i", usedNames));
    }
    return names;
  }

  private static boolean isValidName(NamesValidator namesValidator, String candidate) {
    return namesValidator != null && !namesValidator.isKeyword(candidate, null) && namesValidator.isIdentifier(candidate, null);
  }

  @Nonnull
  private static LinkedHashSet<String> getNamesInContext(PsiElement context) {
    if (context == null) return new LinkedHashSet<>();
    LinkedHashSet<String> names = new LinkedHashSet<>();

    for (GoNamedElement namedElement : PsiTreeUtil.findChildrenOfType(context, GoNamedElement.class)) {
      names.add(namedElement.getName());
    }
    names.addAll(((GoFile) context.getContainingFile()).getImportMap().keySet());

    GoFunctionDeclaration functionDeclaration = PsiTreeUtil.getParentOfType(context, GoFunctionDeclaration.class);
    GoSignature signature = PsiTreeUtil.getChildOfType(functionDeclaration, GoSignature.class);
    for (GoParamDefinition param : PsiTreeUtil.findChildrenOfType(signature, GoParamDefinition.class)) {
      names.add(param.getName());
    }
    return names;
  }
}
