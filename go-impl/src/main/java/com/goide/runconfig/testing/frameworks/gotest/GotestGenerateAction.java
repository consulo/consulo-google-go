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

import com.goide.GoConstants;
import com.goide.generate.GoGenerateTestActionBase;
import com.goide.psi.GoFile;
import com.goide.psi.GoImportSpec;
import com.goide.psi.impl.GoPsiImplUtil;
import com.goide.runconfig.testing.GoTestFunctionType;
import com.goide.template.GoFileLiveTemplateContextType;
import consulo.codeEditor.Editor;
import consulo.codeEditor.util.EditorModificationUtil;
import consulo.component.util.text.UniqueNameGenerator;
import consulo.language.editor.AutoPopupController;
import consulo.language.editor.action.CodeInsightActionHandler;
import consulo.language.editor.template.context.TemplateContextType;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.ui.ex.action.Presentation;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import java.util.Locale;

public class GotestGenerateAction extends GoGenerateTestActionBase {
  public GotestGenerateAction(@Nonnull GoTestFunctionType type) {
    super(GotestFramework.INSTANCE, new GenerateActionHandler(type));
    String presentationName = StringUtil.capitalize(type.toString().toLowerCase(Locale.US));
    Presentation presentation = getTemplatePresentation();
    presentation.setText(presentationName);
    presentation.setDescription("Generate " + presentationName + " function");
  }

  @Override
  protected boolean isValidForFile(@Nonnull Project project, @Nonnull Editor editor, @Nonnull PsiFile file) {
    GoFileLiveTemplateContextType fileContextType =
      TemplateContextType.EP_NAME.findExtension(GoFileLiveTemplateContextType.class);
    return fileContextType != null && fileContextType.isInContext(file, editor.getCaretModel().getOffset());
  }

  @Nonnull
  public static String importTestingPackageIfNeeded(@Nonnull GoFile file) {
    GoImportSpec alreadyImportedPackage = file.getImportedPackagesMap().get(GoConstants.TESTING_PATH);
    String qualifier = GoPsiImplUtil.getImportQualifierToUseInFile(alreadyImportedPackage, GoConstants.TESTING_PATH);
    if (qualifier != null) {
      return qualifier;
    }

    String localName = UniqueNameGenerator.generateUniqueName(GoConstants.TESTING_PATH, file.getImportMap().keySet());
    file.addImport(GoConstants.TESTING_PATH, !GoConstants.TESTING_PATH.equals(localName) ? localName : null);
    return localName;
  }

  private static class GenerateActionHandler implements CodeInsightActionHandler {

    @Nonnull
	private final GoTestFunctionType myType;

    public GenerateActionHandler(@Nonnull GoTestFunctionType type) {
      myType = type;
    }

    @Override
    public void invoke(@Nonnull Project project, @Nonnull Editor editor, @Nonnull PsiFile file) {
      if (!(file instanceof GoFile)) {
        return;
      }
      String testingQualifier = null;
      if (myType.getParamType() != null) {
        testingQualifier = importTestingPackageIfNeeded((GoFile)file);
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
