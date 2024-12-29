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

package com.goide.intentions;

import com.goide.editor.smart.GoSmartEnterProcessor;
import com.goide.psi.GoBlock;
import com.goide.psi.GoFunctionOrMethodDeclaration;
import com.goide.psi.impl.GoElementFactory;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.editor.intention.BaseElementAtCaretIntentionAction;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import consulo.util.lang.ObjectUtil;
import org.jetbrains.annotations.Nls;

import jakarta.annotation.Nonnull;

@ExtensionImpl
@IntentionMetaData(ignoreId = "go.add.function.body", fileExtensions = "go", categories = "Go")
public class GoAddFunctionBlockIntention extends BaseElementAtCaretIntentionAction {
  public static final String NAME = "Add function body";

  public GoAddFunctionBlockIntention() {
    setText(NAME);
  }

  @Override
  public boolean isAvailable(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) {
    PsiElement parent = element.getParent();
    return parent instanceof GoFunctionOrMethodDeclaration && ((GoFunctionOrMethodDeclaration)parent).getBlock() == null;
  }

  @Override
  public void invoke(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) throws IncorrectOperationException {
    PsiElement parent = element.getParent();
    if (parent instanceof GoFunctionOrMethodDeclaration) {
      GoBlock block = ((GoFunctionOrMethodDeclaration)parent).getBlock();
      if (block == null) {
        GoBlock newBlock = ObjectUtil.tryCast(parent.add(GoElementFactory.createBlock(project)), GoBlock.class);
        if (newBlock != null) {
          PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
          new GoSmartEnterProcessor.PlainEnterProcessor().doEnter(newBlock, newBlock.getContainingFile(), editor, false);
        }
      }
    }
  }
}
