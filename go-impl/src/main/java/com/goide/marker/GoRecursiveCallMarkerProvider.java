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
package com.goide.marker;

import com.goide.GoLanguage;
import com.goide.psi.GoCallExpr;
import com.goide.psi.GoFunctionOrMethodDeclaration;
import com.goide.psi.impl.GoPsiImplUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.markup.GutterIconRenderer;
import consulo.document.Document;
import consulo.language.Language;
import consulo.language.editor.Pass;
import consulo.language.editor.gutter.LineMarkerInfo;
import consulo.language.editor.gutter.LineMarkerProvider;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.util.lang.Comparing;

import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ExtensionImpl
public class GoRecursiveCallMarkerProvider implements LineMarkerProvider {
  @Override
  public LineMarkerInfo getLineMarkerInfo(@Nonnull PsiElement element) {
    return null;
  }

  @Override
  public void collectSlowLineMarkers(@Nonnull List<PsiElement> elements, @Nonnull Collection<LineMarkerInfo> result) {
    Set<Integer> lines = new HashSet<>();
    for (PsiElement element : elements) {
      if (element instanceof GoCallExpr) {
        PsiElement resolve = GoPsiImplUtil.resolveCall((GoCallExpr) element);
        if (resolve instanceof GoFunctionOrMethodDeclaration) {
          if (isRecursiveCall(element, (GoFunctionOrMethodDeclaration) resolve)) {
            PsiDocumentManager instance = PsiDocumentManager.getInstance(element.getProject());
            Document document = instance.getDocument(element.getContainingFile());
            int textOffset = element.getTextOffset();
            if (document == null) continue;
            int lineNumber = document.getLineNumber(textOffset);
            if (!lines.contains(lineNumber)) {
              result.add(new RecursiveMethodCallMarkerInfo(element));
            }
            lines.add(lineNumber);
          }
        }
      }
    }
  }

  private static boolean isRecursiveCall(PsiElement element, GoFunctionOrMethodDeclaration function) {
    return Comparing.equal(PsiTreeUtil.getParentOfType(element, GoFunctionOrMethodDeclaration.class), function);
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return GoLanguage.INSTANCE;
  }

  private static class RecursiveMethodCallMarkerInfo extends LineMarkerInfo<PsiElement> {
    private RecursiveMethodCallMarkerInfo(@Nonnull PsiElement methodCall) {
      super(methodCall,
          methodCall.getTextRange(),
          PlatformIconGroup.gutterRecursivemethod(),
          Pass.LINE_MARKERS,
          (e) -> "Recursive call",
          null,
          GutterIconRenderer.Alignment.RIGHT
      );
    }
  }
}


