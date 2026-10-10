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

package com.goide.runconfig.testing.frameworks.gotest;

import com.goide.generate.GoGenerateTestActionBase;
import com.goide.psi.GoFile;
import com.goide.template.GoFileLiveTemplateContextType;
import consulo.codeEditor.Editor;
import consulo.codeEditor.util.EditorModificationUtil;
import consulo.go.language.testing.GoTestFinder;
import consulo.go.language.testing.GoTestFunctionType;
import consulo.language.editor.AutoPopupController;
import consulo.language.editor.action.CodeInsightActionHandler;
import consulo.language.editor.template.context.TemplateContextType;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.ui.ex.action.Presentation;
import consulo.util.lang.StringUtil;

import java.util.Locale;

public class GotestGenerateAction extends GoGenerateTestActionBase {
  public GotestGenerateAction(GoTestFunctionType type) {
    super(GotestFramework.INSTANCE, new GenerateActionHandler(type));
    String presentationName = StringUtil.capitalize(type.toString().toLowerCase(Locale.US));
    Presentation presentation = getTemplatePresentation();
    presentation.setText(presentationName);
    presentation.setDescription("Generate " + presentationName + " function");
  }

  @Override
  protected boolean isValidForFile(Project project, Editor editor, PsiFile file) {
    GoFileLiveTemplateContextType fileContextType =
      TemplateContextType.EP_NAME.findExtension(GoFileLiveTemplateContextType.class);
    return fileContextType != null && fileContextType.isInContext(file, editor.getCaretModel().getOffset());
  }

  private static class GenerateActionHandler implements CodeInsightActionHandler {

	private final GoTestFunctionType myType;

    public GenerateActionHandler(GoTestFunctionType type) {
      myType = type;
    }

    @Override
    public void invoke(Project project, Editor editor, PsiFile file) {
      if (!(file instanceof GoFile)) {
        return;
      }
      String testingQualifier = null;
      if (myType.getParamType() != null) {
        testingQualifier = GoTestFinder.importTestingPackageIfNeeded((GoFile)file);
        PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
      }
      String functionText = "func " + myType.getPrefix();
      int offset = functionText.length();
      functionText += "(" + myType.getSignature(testingQualifier) + ") {\n\t\n}";
      EditorModificationUtil.insertStringAtCaret(editor, functionText, false, offset);
      AutoPopupController.getInstance(project).scheduleAutoPopup(editor);
    }

    @Override
    public boolean startInWriteAction() {
      return true;
    }
  }
}
