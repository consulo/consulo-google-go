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

package com.goide.tree;

import com.goide.psi.GoNamedElement;
import consulo.application.AllIcons;
import consulo.fileEditor.structureView.tree.ActionPresentation;
import consulo.fileEditor.structureView.tree.ActionPresentationData;
import consulo.fileEditor.structureView.tree.Filter;
import consulo.fileEditor.structureView.tree.TreeElement;
import consulo.language.psi.PsiElement;

import javax.annotation.Nonnull;

public class GoPrivateMembersFilter implements Filter {
  private final static String PRIVATE_MEMBERS_FILTER_TEXT = "Show Private Members";

  @Override
  public boolean isVisible(TreeElement treeNode) {
    if (treeNode instanceof GoStructureViewFactory.Element) {
      PsiElement psiElement = ((GoStructureViewFactory.Element)treeNode).getValue();
      return !(psiElement instanceof GoNamedElement) || ((GoNamedElement)psiElement).isPublic();
    }
    return true;
  }

  @Override
  public boolean isReverted() {
    return true;
  }

  @Nonnull
  @Override
  public ActionPresentation getPresentation() {
    return new ActionPresentationData(PRIVATE_MEMBERS_FILTER_TEXT, null, AllIcons.Nodes.C_private);
  }

  @Nonnull
  @Override
  public String getName() {
    return PRIVATE_MEMBERS_FILTER_TEXT;
  }
}
