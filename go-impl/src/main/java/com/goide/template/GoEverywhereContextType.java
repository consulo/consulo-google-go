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

package com.goide.template;

import com.goide.GoTypes;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.template.context.EverywhereContextType;
import consulo.language.impl.psi.LeafPsiElement;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;

@ExtensionImpl
public class GoEverywhereContextType extends GoLiveTemplateContextType {
  @Inject
  protected GoEverywhereContextType() {
    super("GO", LocalizeValue.localizeTODO("Go"), EverywhereContextType.class);
  }

  @Override
  protected boolean isInContext(@Nonnull PsiElement element) {
    return !(element instanceof PsiComment ||
             element instanceof LeafPsiElement && ((LeafPsiElement)element).getElementType() == GoTypes.STRING);
  }
}
