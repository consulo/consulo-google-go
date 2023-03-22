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

import com.goide.GoConstants;
import com.goide.GoDocumentationProvider;
import com.goide.psi.GoCompositeElement;
import com.goide.psi.GoNamedElement;
import com.goide.psi.GoPackageClause;
import com.goide.psi.GoVisitor;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiComment;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * golint inspection from:
 * https://github.com/golang/lint/blob/32a87160691b3c96046c0c678fe57c5bef761456/lint.go#L744
 */
@ExtensionImpl
public class GoCommentStartInspection extends GoInspectionBase {
  @Nonnull
  @Override
  protected GoVisitor buildGoVisitor(@Nonnull ProblemsHolder holder, @Nonnull LocalInspectionToolSession session, Object inspectionState) {
    return new GoVisitor() {

      @Override
      public void visitPackageClause(@Nonnull GoPackageClause o) {
        String packageName = o.getName();
        if (GoConstants.MAIN.equals(packageName)) {
          return;
        }
        List<PsiComment> comments = GoDocumentationProvider.getCommentsForElement(o);
        String commentText = GoDocumentationProvider.getCommentText(comments, false);
        if (!comments.isEmpty() && !commentText.isEmpty() && !commentText.startsWith("Package " + packageName)) {
          registerProblem(comments, "Package comment should be of the form 'Package " + packageName + " ...'", holder);
        }
      }

      @Override
      public void visitCompositeElement(@Nonnull GoCompositeElement o) {
        if (!(o instanceof GoNamedElement) || !((GoNamedElement)o).isPublic()) {
          return;
        }
        List<PsiComment> comments = GoDocumentationProvider.getCommentsForElement(o);
        String commentText = GoDocumentationProvider.getCommentText(comments, false);
        String elementName = ((GoNamedElement)o).getName();
        if (elementName != null && !comments.isEmpty() && !commentText.isEmpty()) {
          if (!isCorrectComment(commentText, elementName)) {
            registerProblem(comments, "Comment should start with '" + elementName + "'", holder);
          }
          // +1 stands for Element_Name<space>
          else if (commentText.length() <= elementName.length() + 1) {
            registerProblem(comments, "Comment should be meaningful or it should be removed", holder);
          }
        }
      }
    };
  }

  private static void registerProblem(List<PsiComment> comments, String description, @Nonnull ProblemsHolder holder) {
    for (PsiComment comment : comments) {
      holder.registerProblem(comment, description, ProblemHighlightType.WEAK_WARNING);
    }
  }

  private static boolean isCorrectComment(String commentText, String elementName) {
    return commentText.startsWith(elementName)
           || commentText.startsWith("A " + elementName)
           || commentText.startsWith("An " + elementName)
           || commentText.startsWith("The " + elementName);
  }

  @Override
  public boolean isEnabledByDefault() {
    return false;
  }

  @Nonnull
  @Override
  public String getGroupDisplayName() {
    return "Code style issues";
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Comment of exported element starts with incorrect name";
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WEAK_WARNING;
  }
}
