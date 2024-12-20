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

package com.goide.runconfig;

import com.goide.GoConstants;
import com.goide.GoLanguage;
import com.goide.GoTypes;
import com.goide.psi.GoFunctionDeclaration;
import consulo.annotation.component.ExtensionImpl;
import consulo.execution.icon.ExecutionIconGroup;
import consulo.execution.lineMarker.ExecutorAction;
import consulo.execution.lineMarker.RunLineMarkerContributor;
import consulo.language.Language;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

@ExtensionImpl
public class GoRunLineMarkerProvider extends RunLineMarkerContributor {
  private static final Function<PsiElement, String> TOOLTIP_PROVIDER = element -> "Run Application";

  @Nullable
  @Override
  public Info getInfo(PsiElement e) {
    if (e != null && e.getNode().getElementType() == GoTypes.IDENTIFIER) {
      PsiElement parent = e.getParent();
      PsiFile file = e.getContainingFile();
      if (GoRunUtil.isMainGoFile(file) && parent instanceof GoFunctionDeclaration) {
        if (GoConstants.MAIN.equals(((GoFunctionDeclaration)parent).getName())) {
          return new Info(ExecutionIconGroup.gutterRun(), TOOLTIP_PROVIDER, ExecutorAction.getActions(0));
        }
      }
    }
    return null;
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return GoLanguage.INSTANCE;
  }
}
