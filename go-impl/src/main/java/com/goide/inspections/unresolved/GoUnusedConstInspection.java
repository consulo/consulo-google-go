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

package com.goide.inspections.unresolved;

import javax.annotation.Nonnull;

import com.goide.inspections.GoInspectionBase;
import com.goide.psi.GoConstDefinition;
import com.goide.psi.GoVisitor;
import com.goide.quickfix.GoDeleteConstDefinitionQuickFix;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.search.searches.ReferencesSearch;

public class GoUnusedConstInspection extends GoInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session) {
    return new GoVisitor() {
      @Override
      public void visitConstDefinition(@Nonnull GoConstDefinition o) {
        if (o.isBlank()) return;
        if (ReferencesSearch.search(o, o.getUseScope()).findFirst() == null) {
          String constName = o.getName();
          holder.registerProblem(o, "Unused constant <code>#ref</code> #loc", ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                                 new GoDeleteConstDefinitionQuickFix(constName));
        }
      }
    };
  }
}
