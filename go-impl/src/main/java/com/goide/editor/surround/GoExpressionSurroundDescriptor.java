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

package com.goide.editor.surround;

import com.goide.GoLanguage;
import com.goide.psi.GoExpression;
import com.goide.refactor.GoIntroduceVariableBase;
import consulo.annotation.component.ExtensionImpl;
import consulo.externalService.statistic.FeatureUsageTracker;
import consulo.externalService.statistic.UsageTrigger;
import consulo.language.Language;
import consulo.language.editor.surroundWith.SurroundDescriptor;
import consulo.language.editor.surroundWith.Surrounder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;

import javax.annotation.Nonnull;

@ExtensionImpl
public class GoExpressionSurroundDescriptor implements SurroundDescriptor {
  private static final Surrounder[] SURROUNDERS = new Surrounder[]{
    new GoWithParenthesisSurrounder(),
    new GoWithIfExpressionSurrounder(),
    new GoWithIfElseExpressionSurrounder(),
    new GoWithNotExpressionSurrounder()
  };

  @Nonnull
  @Override
  public Surrounder[] getSurrounders() {
    return SURROUNDERS;
  }

  @Nonnull
  @Override
  public PsiElement[] getElementsToSurround(PsiFile file, int startOffset, int endOffset) {
    GoExpression expr = GoIntroduceVariableBase.findExpressionInSelection(file, startOffset, endOffset);
    if (expr == null) return PsiElement.EMPTY_ARRAY;
    UsageTrigger.trigger("go.surroundwith.expression");
    FeatureUsageTracker.getInstance().triggerFeatureUsed("codeassists.surroundwith.expression");
    return new PsiElement[]{expr};
  }

  @Override
  public boolean isExclusive() {
    return false;
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return GoLanguage.INSTANCE;
  }
}
