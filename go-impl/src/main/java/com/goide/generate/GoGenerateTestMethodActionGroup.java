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

package com.goide.generate;

import com.goide.runconfig.testing.GoTestFramework;
import consulo.annotation.component.ActionImpl;
import consulo.annotation.component.ActionParentRef;
import consulo.annotation.component.ActionRef;
import consulo.annotation.component.ActionRefAnchor;
import consulo.codeEditor.Editor;
import consulo.language.editor.CommonDataKeys;
import consulo.language.editor.util.PsiUtilBase;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.ui.ex.action.ActionGroup;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.IdeActions;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@ActionImpl(id = "GoTestGenerateGroup", parents = @ActionParentRef(value = @ActionRef(id = IdeActions.GROUP_GENERATE), anchor = ActionRefAnchor.FIRST))
public class GoGenerateTestMethodActionGroup extends ActionGroup {
  @Nonnull
  @Override
  public AnAction[] getChildren(@Nullable AnActionEvent e) {
    if (e == null) {
      return AnAction.EMPTY_ARRAY;
    }
    Project project = e.getData(Project.KEY);
    Editor editor = e.getData(CommonDataKeys.EDITOR);
    if (project == null || editor == null) return AnAction.EMPTY_ARRAY;
    PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);

    List<AnAction> children = new ArrayList<>();
    for (GoTestFramework framework : GoTestFramework.all()) {
      if (framework.isAvailableOnFile(file)) {
        children.addAll(framework.getGenerateMethodActions());
      }
    }
    return !children.isEmpty() ? children.toArray(new AnAction[children.size()]) : AnAction.EMPTY_ARRAY;
  }
}
