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

import com.goide.GoTypes;
import com.goide.psi.GoCallExpr;
import com.goide.psi.GoReferenceExpression;
import com.goide.psi.impl.GoPsiImplUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.editor.highlight.usage.HighlightUsagesHandlerBase;
import consulo.language.editor.highlight.usage.HighlightUsagesHandlerFactoryBase;
import consulo.language.impl.psi.LeafPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class GoHighlightExitPointsHandlerFactory extends HighlightUsagesHandlerFactoryBase {
  private static final TokenSet BREAK_HIGHLIGHTING_TOKENS = TokenSet.create(GoTypes.BREAK, GoTypes.SWITCH, GoTypes.FOR, GoTypes.SELECT);

  @RequiredReadAction
  @Nullable
  @Override
  public HighlightUsagesHandlerBase createHighlightUsagesHandler(@Nonnull Editor editor,
                                                                 @Nonnull PsiFile file,
                                                                 @Nonnull PsiElement target) {
    if (target instanceof LeafPsiElement) {
      IElementType elementType = ((LeafPsiElement) target).getElementType();
      if (elementType == GoTypes.RETURN || elementType == GoTypes.FUNC || isPanicCall(target)) {
        return GoFunctionExitPointHandler.createForElement(editor, file, target);
      } else if (BREAK_HIGHLIGHTING_TOKENS.contains(elementType)) {
        return GoBreakStatementExitPointHandler.createForElement(editor, file, target);
      }
    }
    return null;
  }

  private static boolean isPanicCall(@Nonnull PsiElement e) {
    PsiElement parent = e.getParent();
    if (parent instanceof GoReferenceExpression) {
      PsiElement grandPa = parent.getParent();
      if (grandPa instanceof GoCallExpr) return GoPsiImplUtil.isPanic((GoCallExpr) grandPa);
    }
    return false;
  }
}