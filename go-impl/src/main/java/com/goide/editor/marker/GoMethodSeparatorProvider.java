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

package com.goide.editor.marker;

import com.goide.GoLanguage;
import com.goide.psi.GoFile;
import com.goide.psi.GoTopLevelDeclaration;
import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.EditorColorsManager;
import consulo.language.Language;
import consulo.language.editor.DaemonCodeAnalyzerSettings;
import consulo.language.editor.gutter.LineMarkerInfo;
import consulo.language.editor.gutter.LineMarkerProvider;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiWhiteSpace;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ExtensionImpl
public class GoMethodSeparatorProvider implements LineMarkerProvider {
  private final DaemonCodeAnalyzerSettings myDaemonSettings;
  private final EditorColorsManager myColorsManager;

  @Inject
  public GoMethodSeparatorProvider(DaemonCodeAnalyzerSettings daemonSettings, EditorColorsManager colorsManager) {
    myDaemonSettings = daemonSettings;
    myColorsManager = colorsManager;
  }

  @Nullable
  @Override
  public LineMarkerInfo getLineMarkerInfo(@Nonnull PsiElement o) {
    if (myDaemonSettings.SHOW_METHOD_SEPARATORS && o instanceof GoTopLevelDeclaration && o.getParent() instanceof GoFile) {
      return LineMarkerInfo.createMethodSeparatorLineMarker(findAnchorElement((GoTopLevelDeclaration) o), myColorsManager);
    }
    return null;
  }

  @Nonnull
  private static PsiElement findAnchorElement(@Nonnull GoTopLevelDeclaration o) {
    PsiElement result = o;
    PsiElement p = o;
    while ((p = p.getPrevSibling()) != null) {
      if (p instanceof PsiComment) {
        result = p;
      } else if (p instanceof PsiWhiteSpace) {
        if (p.getText().contains("\n\n")) return result;
      } else {
        break;
      }
    }
    return result;
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return GoLanguage.INSTANCE;
  }
}