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

package com.goide.actions.file;

import com.goide.psi.GoFile;
import com.goide.psi.GoPackageClause;
import consulo.annotation.component.ActionImpl;
import consulo.annotation.component.ActionParentRef;
import consulo.annotation.component.ActionRef;
import consulo.annotation.component.ActionRefAnchor;
import consulo.application.dumb.DumbAware;
import consulo.codeEditor.Editor;
import consulo.document.FileDocumentManager;
import consulo.fileEditor.FileEditorManager;
import consulo.google.go.icon.GoogleGoIconGroup;
import consulo.google.go.module.extension.GoModuleExtension;
import consulo.ide.action.CreateFileFromTemplateAction;
import consulo.ide.action.CreateFileFromTemplateDialog;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiFile;
import consulo.module.extension.ModuleExtension;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

@ActionImpl(id = "Go.NewGoFile", parents = @ActionParentRef(value = @ActionRef(id = "NewGroup"), anchor = ActionRefAnchor.BEFORE, relatedToAction = @ActionRef(id = "NewFile")))
public class GoCreateFileAction extends CreateFileFromTemplateAction implements DumbAware {
  public static final String FILE_TEMPLATE = "Go File";
  public static final String APPLICATION_TEMPLATE = "Go Application";

  private static final String DEFAULT_GO_TEMPLATE_PROPERTY = "DefaultGoTemplateProperty";

  public GoCreateFileAction() {
    super("Go File", "", GoogleGoIconGroup.gofiletype());
  }

  @Override
  protected void buildDialog(Project project, PsiDirectory directory, @Nonnull CreateFileFromTemplateDialog.Builder builder) {
    builder.setTitle("New Go File")
      .addKind("Empty file", GoogleGoIconGroup.gofiletype(), FILE_TEMPLATE)
      .addKind("Simple Application", GoogleGoIconGroup.gofiletype(), APPLICATION_TEMPLATE);
  }

  @Nullable
  @Override
  protected String getDefaultTemplateProperty() {
    return DEFAULT_GO_TEMPLATE_PROPERTY;
  }

  @Nonnull
  @Override
  protected String getActionName(PsiDirectory directory, String newName, String templateName) {
    return "New Go File";
  }

  @Override
  protected void postProcess(PsiFile createdElement, String templateName, Map<String, String> customProperties) {
    if (createdElement instanceof GoFile) {
      GoPackageClause packageClause = ((GoFile)createdElement).getPackage();
      if (packageClause == null) {
        return;
      }
      Project project = createdElement.getProject();
      Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
      if (editor == null) {
        return;
      }
      VirtualFile virtualFile = createdElement.getContainingFile().getVirtualFile();
      if (virtualFile == null) {
        return;
      }
      if (FileDocumentManager.getInstance().getDocument(virtualFile) == editor.getDocument()) {
        editor.getCaretModel().moveToOffset(packageClause.getTextRange().getEndOffset());
      }
    }
  }

  @Nullable
  @Override
  protected Class<? extends ModuleExtension> getModuleExtensionClass() {
    return GoModuleExtension.class;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof GoCreateFileAction;
  }
}