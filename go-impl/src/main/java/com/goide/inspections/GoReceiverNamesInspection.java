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

package com.goide.inspections;

import com.goide.psi.GoReceiver;
import com.goide.psi.GoVisitor;
import com.goide.quickfix.GoRenameQuickFix;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;

import javax.annotation.Nonnull;
import java.util.Set;

@ExtensionImpl
public class GoReceiverNamesInspection extends GoInspectionBase {
  private static final Set<String> genericNamesSet = Set.of("me", "this", "self");

  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session, Object inspectionState) {
    return new GoVisitor() {
      @Override
      public void visitReceiver(@Nonnull GoReceiver o) {
        if (genericNamesSet.contains(o.getName())) {
          PsiElement identifier = o.getIdentifier();
          if (identifier == null) return;
          holder.registerProblem(identifier, "Receiver has generic name", new GoRenameQuickFix(o));
        }
      }
    };
  }

  @Nonnull
  @Override
  public String getGroupDisplayName() {
    return "Code style issues";
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Receiver has generic name";
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WEAK_WARNING;
  }
}
