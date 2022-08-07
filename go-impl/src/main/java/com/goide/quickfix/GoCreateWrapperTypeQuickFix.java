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

package com.goide.quickfix;

import com.goide.psi.GoType;
import com.goide.psi.GoTypeDeclaration;
import com.goide.psi.GoTypeSpec;
import com.goide.psi.impl.GoElementFactory;
import consulo.codeEditor.Editor;
import consulo.language.editor.CodeInsightUtilCore;
import consulo.language.editor.inspection.LocalQuickFixAndIntentionActionOnPsiElement;
import consulo.language.editor.template.*;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import org.jetbrains.annotations.Nls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GoCreateWrapperTypeQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {

  private static final String INPUT_NAME = "INPUTVAR";
  private static final String OTHER_NAME = "OTHERVAR";

  public static final String QUICKFIX_NAME = "Create type";

  public GoCreateWrapperTypeQuickFix(@Nonnull GoType type) {
    super(type);
  }

  @Override
  public void invoke(@Nonnull Project project,
                     @Nonnull PsiFile file,
                     @Nullable Editor editor,
                     @Nonnull PsiElement startElement,
                     @Nonnull PsiElement endElement) {
    if (!(startElement instanceof GoType)) return;
    GoType type = (GoType)startElement;

    PsiElement anchor = PsiTreeUtil.findPrevParent(file, type);

    String name = "TypeName";
    GoTypeDeclaration decl = (GoTypeDeclaration)file.addBefore(GoElementFactory.createTypeDeclaration(project, name, type), anchor);
    if (decl == null) return;
    decl = CodeInsightUtilCore.forcePsiPostprocessAndRestoreElement(decl);
    if (decl == null) return;
    GoTypeSpec spec = ContainerUtil.getFirstItem(decl.getTypeSpecList());
    if (spec == null) return;

    TemplateBuilder builder = TemplateBuilderFactory.getInstance().createTemplateBuilder(file);
    builder.replaceElement(type, OTHER_NAME, INPUT_NAME, false);
    builder.replaceElement(spec.getIdentifier(), INPUT_NAME, new ConstantNode(name), true);

    editor.getCaretModel().moveToOffset(file.getTextRange().getStartOffset());
    Template template = builder.buildInlineTemplate();
    editor.getCaretModel().moveToOffset(file.getTextRange().getStartOffset());
    TemplateManager.getInstance(project).startTemplate(editor, template);
  }

  @Nonnull
  @Override
  public String getText() {
    return QUICKFIX_NAME;
  }

  @Nls
  @Nonnull
  @Override
  public String getFamilyName() {
    return QUICKFIX_NAME;
  }
}
