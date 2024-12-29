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

public class GoBreakStatementExitPointHandler extends HighlightUsagesHandlerBase<PsiElement> {
  @Nonnull
  private final PsiElement myTarget;
  @Nullable private final GoBreakStatement myBreakStatement;
  @Nullable
  private final PsiElement myOwner;

  private GoBreakStatementExitPointHandler(@Nonnull Editor editor,
                                           @Nonnull PsiFile file,
                                           @Nonnull PsiElement target,
                                           @Nullable GoBreakStatement breakStatement,
                                           @Nullable PsiElement owner) {
    super(editor, file);
    myTarget = target;
    myBreakStatement = breakStatement;
    myOwner = owner;
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
    PsiElement breakStmtOwner = findBreakStatementOwner();
    GoRecursiveVisitor visitor = new GoRecursiveVisitor() {
      @Override
      public void visitLabelDefinition(@Nonnull GoLabelDefinition o) {
        if (o == breakStmtOwner) {
          addOccurrence(o);
        }
        super.visitLabelDefinition(o);
      }

      @Override
      public void visitBreakStatement(@Nonnull GoBreakStatement o) {
        if (o == myBreakStatement || getBreakStatementOwnerOrResolve(o) == breakStmtOwner) {
          addOccurrence(o);
        }
        super.visitBreakStatement(o);
      }

      @Override
      public void visitSwitchStatement(@Nonnull GoSwitchStatement o) {
        if (o == breakStmtOwner) {
          GoSwitchStart switchStart = o.getSwitchStart();
          if (switchStart != null) {
            addOccurrence(switchStart.getSwitch());
          }
        }
        super.visitSwitchStatement(o);
      }

      @Override
      public void visitForStatement(@Nonnull GoForStatement o) {
        if (o == breakStmtOwner) {
          addOccurrence(o.getFor());
        }
        super.visitForStatement(o);
      }

      @Override
      public void visitSelectStatement(@Nonnull GoSelectStatement o) {
        if (o == breakStmtOwner) {
          addOccurrence(o.getSelect());
        }
        super.visitSelectStatement(o);
      }
    };
    if (breakStmtOwner != null) {
      PsiElement parent = breakStmtOwner.getParent();
      if (parent instanceof GoCompositeElement) {
        visitor.visitCompositeElement((GoCompositeElement)parent);
      }
    }
  }

  @Nullable
  private PsiElement findBreakStatementOwner() {
    if (myOwner != null) return myOwner;
    if (myBreakStatement != null) return getBreakStatementOwnerOrResolve(myBreakStatement);
    return null;
  }

  @Nullable
  public static PsiElement getBreakStatementOwnerOrResolve(@Nonnull GoBreakStatement breakStatement) {
    GoLabelRef label = breakStatement.getLabelRef();
    if (label != null) {
      return label.getReference().resolve();
    }
    return GoPsiImplUtil.getBreakStatementOwner(breakStatement);
  }

  @Nullable
  public static GoBreakStatementExitPointHandler createForElement(@Nonnull Editor editor,
                                                                  @Nonnull PsiFile file,
                                                                  @Nonnull PsiElement element) {
    PsiElement target = PsiTreeUtil.getParentOfType(element, GoBreakStatement.class, GoSwitchStatement.class, GoSelectStatement.class,
                                                    GoForStatement.class);
    if (target == null) {
      return null;
    }
    return target instanceof GoBreakStatement
           ? new GoBreakStatementExitPointHandler(editor, file, element, (GoBreakStatement)target, null)
           : new GoBreakStatementExitPointHandler(editor, file, element, null, target);
  }
}
