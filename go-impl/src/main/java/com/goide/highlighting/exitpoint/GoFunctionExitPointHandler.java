/*
 * Copyright 2013-2015 Sergey Ignatov, Alexander Zolotov, Florin Patan
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

package com.goide.highlighting.exitpoint;

import com.goide.psi.*;
import com.goide.psi.impl.GoPsiImplUtil;
import consulo.codeEditor.Editor;
import consulo.language.editor.highlight.usage.HighlightUsagesHandlerBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class GoFunctionExitPointHandler extends HighlightUsagesHandlerBase<PsiElement> {
  @Nonnull
  private final PsiElement myTarget;
  @Nonnull
  private final GoTypeOwner myFunction;

  private GoFunctionExitPointHandler(Editor editor, PsiFile file, @Nonnull PsiElement target, @Nonnull GoTypeOwner function) {
    super(editor, file);
    myTarget = target;
    myFunction = function;
  }

  @Nonnull
  @Override
  public List<PsiElement> getTargets() {
    return Collections.singletonList(myTarget);
  }

  @Override
  protected void selectTargets(List<PsiElement> targets, @Nonnull Consumer<List<PsiElement>> selectionConsumer) {
    selectionConsumer.accept(targets);
  }

  @Override
  public void computeUsages(List<PsiElement> targets) {
    if (myFunction instanceof GoFunctionOrMethodDeclaration) {
      addOccurrence(((GoFunctionOrMethodDeclaration)myFunction).getFunc());
    }
    new GoRecursiveVisitor() {
      @Override
      public void visitFunctionLit(@Nonnull GoFunctionLit literal) {
      }

      @Override
      public void visitReturnStatement(@Nonnull GoReturnStatement statement) {
        addOccurrence(statement);
      }

      @Override
      public void visitCallExpr(@Nonnull GoCallExpr o) {
        if (GoPsiImplUtil.isPanic(o)) addOccurrence(o);
        super.visitCallExpr(o);
      }
    }.visitTypeOwner(myFunction);
  }

  @Nullable
  public static GoFunctionExitPointHandler createForElement(@Nonnull Editor editor, @Nonnull PsiFile file, @Nonnull PsiElement element) {
    GoTypeOwner function = PsiTreeUtil.getParentOfType(element, GoFunctionLit.class, GoFunctionOrMethodDeclaration.class);
    return function != null ? new GoFunctionExitPointHandler(editor, file, element, function) : null;
  }
}
