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

package com.goide.codeInsight.imports;

import com.goide.project.GoExcludedPathsSettings;
import com.goide.psi.GoFile;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.AllIcons;
import consulo.application.ApplicationManager;
import consulo.ide.setting.ShowSettingsUtil;
import consulo.language.editor.completion.lookup.Lookup;
import consulo.language.editor.completion.lookup.LookupActionProvider;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

@ExtensionImpl
public class GoExcludePathLookupActionProvider implements LookupActionProvider {
  @Override
  public void fillActions(LookupElement element, Lookup lookup, Consumer<LookupElementAction> consumer) {
    PsiElement psiElement = element.getPsiElement();
    PsiFile file = psiElement != null && psiElement.isValid() ? psiElement.getContainingFile() : null;
    String importPath = file instanceof GoFile ? ((GoFile) file).getImportPath(false) : null;
    if (importPath != null) {
      Project project = psiElement.getProject();
      for (String path : getPaths(importPath)) {
        consumer.accept(new ExcludePathAction(project, path));
      }
      consumer.accept(new EditExcludedAction(project));
    }
  }

  private static List<String> getPaths(String importPath) {
    List<String> result = ContainerUtil.newArrayList(importPath);
    int i;
    while ((i = importPath.lastIndexOf('/')) > 0) {
      importPath = importPath.substring(0, i);
      result.add(importPath);
    }
    return result;
  }

  private static class EditExcludedAction extends LookupElementAction {
    @Nonnull
    Project myProject;

    protected EditExcludedAction(@Nonnull Project project) {
      super(AllIcons.Actions.Edit, "Edit auto import settings");
      myProject = project;
    }

    @Override
    public Result performLookupAction() {
      ApplicationManager.getApplication().invokeLater(() -> {
        ShowSettingsUtil.getInstance().showAndSelect(myProject, GoAutoImportConfigurable.class, it -> it.focusList());
      });
      return Result.HIDE_LOOKUP;
    }
  }

  private static class ExcludePathAction extends LookupElementAction {
    private Project myProject;
    private String myImportPath;

    protected ExcludePathAction(@Nonnull Project project, @Nonnull String importPath) {
      super(AllIcons.Actions.Exclude, "Exclude '" + importPath + "'");
      myProject = project;
      myImportPath = importPath;
    }

    @Override
    public Result performLookupAction() {
      GoExcludedPathsSettings.getInstance(myProject).excludePath(myImportPath);
      return Result.HIDE_LOOKUP;
    }
  }
}
