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

package com.goide.inspections.suppression;

import com.goide.GoLanguage;
import com.goide.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.inspection.AbstractBatchSuppressByNoInspectionCommentFix;
import consulo.language.editor.inspection.InspectionSuppressor;
import consulo.language.editor.inspection.SuppressQuickFix;
import consulo.language.editor.inspection.SuppressionUtil;
import consulo.language.psi.ElementDescriptionUtil;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.usage.UsageViewTypeLocation;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Matcher;

@ExtensionImpl
public class GoInspectionSuppressor implements InspectionSuppressor {
  @Override
  public boolean isSuppressedFor(@Nonnull PsiElement element, @Nonnull String toolId) {
    GoTopLevelDeclaration topLevelDeclaration = PsiTreeUtil.getTopmostParentOfType(element, GoTopLevelDeclaration.class);
    if (topLevelDeclaration != null && isSuppressedInStatement(toolId, topLevelDeclaration)) {
      return true;
    }
    GoImportDeclaration importDeclaration = PsiTreeUtil.getNonStrictParentOfType(element, GoImportDeclaration.class);
    if (importDeclaration != null && importDeclaration.getPrevSibling() == null
        && isSuppressedInStatement(element, toolId, GoImportList.class)) {
      return true;
    }
    return isSuppressedInStatement(element, toolId, GoPackageClause.class, GoStatement.class, GoCommClause.class, GoCaseClause.class,
                                   GoTopLevelDeclaration.class, GoImportDeclaration.class);
  }

  @Nonnull
  @Override
  public SuppressQuickFix[] getSuppressActions(PsiElement element, @Nonnull String toolId) {
    return new SuppressQuickFix[]{
      new GoSuppressInspectionFix("comm case", GoCommClause.class, false),
      new GoSuppressInspectionFix(toolId, "comm case", GoCommClause.class, false),
      new GoSuppressInspectionFix("case", GoCaseClause.class, false),
      new GoSuppressInspectionFix(toolId, "case", GoCaseClause.class, false),
      new GoSuppressInspectionFix("declaration", GoTopLevelDeclaration.class, true),
      new GoSuppressInspectionFix(toolId, "declaration", GoTopLevelDeclaration.class, true),
      new GoSuppressForStatementFix(),
      new GoSuppressForStatementFix(toolId),
      new GoSuppressInspectionFix("import", GoImportDeclaration.class, false),
      new GoSuppressInspectionFix(toolId, "import", GoImportDeclaration.class, false),
      new GoSuppressInspectionFix("package statement", GoPackageClause.class, false),
      new GoSuppressInspectionFix(toolId, "package statement", GoPackageClause.class, false),
    };
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return GoLanguage.INSTANCE;
  }

  private static class GoSuppressForStatementFix extends GoSuppressInspectionFix {
    public GoSuppressForStatementFix() {
      super("statement", GoStatement.class, false);
    }

    public GoSuppressForStatementFix(@Nonnull String ID) {
      super(ID, "statement", GoStatement.class, false);
    }

    @Nullable
    @Override
    public PsiElement getContainer(PsiElement context) {
      GoStatement statement = PsiTreeUtil.getNonStrictParentOfType(context, GoStatement.class);
      if (statement != null && statement.getParent() instanceof GoCommCase) {
        return PsiTreeUtil.getParentOfType(statement, GoStatement.class);
      }
      return statement;
    }
  }

  public static class GoSuppressInspectionFix extends AbstractBatchSuppressByNoInspectionCommentFix {
    private final Class<? extends GoCompositeElement> myContainerClass;
    private final String myBaseText;
    private final boolean myTopMost;

    public GoSuppressInspectionFix(@Nonnull String elementDescription,
                                   Class<? extends GoCompositeElement> containerClass,
                                   boolean topMost) {
      super(SuppressionUtil.ALL, true);
      myBaseText = "Suppress all inspections for ";
      setText(myBaseText + elementDescription);
      myContainerClass = containerClass;
      myTopMost = topMost;
    }

    public GoSuppressInspectionFix(@Nonnull String ID,
                                   @Nonnull String elementDescription,
                                   Class<? extends GoCompositeElement> containerClass,
                                   boolean topMost) {
      super(ID, false);
      myBaseText = "Suppress for ";
      setText(myBaseText + elementDescription);
      myTopMost = topMost;
      myContainerClass = containerClass;
    }

    @Override
    @Nullable
    public PsiElement getContainer(PsiElement context) {
      PsiElement container;
      if (myTopMost) {
        container = PsiTreeUtil.getTopmostParentOfType(context, myContainerClass);
        if (container == null && myContainerClass.isInstance(context)) {
          container = context;
        }
      }
      else {
        container = PsiTreeUtil.getNonStrictParentOfType(context, myContainerClass);
      }
      if (container != null) {
        String description = ElementDescriptionUtil.getElementDescription(container, UsageViewTypeLocation.INSTANCE);
        if (StringUtil.isNotEmpty(description)) {
          setText(myBaseText + description);
        }
      }
      return container;
    }
  }

  private static boolean isSuppressedInStatement(@Nonnull PsiElement place,
                                                 @Nonnull String toolId,
                                                 @Nonnull Class<? extends PsiElement>... statementClasses) {
    PsiElement statement = PsiTreeUtil.getNonStrictParentOfType(place, statementClasses);
    while (statement != null) {
      if (isSuppressedInStatement(toolId, statement)) {
        return true;
      }
      statement = PsiTreeUtil.getParentOfType(statement, statementClasses);
    }
    return false;
  }

  private static boolean isSuppressedInStatement(@Nonnull String toolId, @Nullable PsiElement statement) {
    if (statement != null) {
      PsiElement prev = PsiTreeUtil.skipSiblingsBackward(statement, PsiWhiteSpace.class);
      if (prev instanceof PsiComment) {
        String text = prev.getText();
        Matcher matcher = SuppressionUtil.SUPPRESS_IN_LINE_COMMENT_PATTERN.matcher(text);
        return matcher.matches() && SuppressionUtil.isInspectionToolIdMentioned(matcher.group(1), toolId);
      }
    }
    return false;
  }
}
