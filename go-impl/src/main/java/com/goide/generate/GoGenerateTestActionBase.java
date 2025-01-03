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
import consulo.codeEditor.Editor;
import consulo.language.editor.action.CodeInsightAction;
import consulo.language.editor.action.CodeInsightActionHandler;
import consulo.language.psi.PsiFile;
import consulo.project.Project;

import jakarta.annotation.Nonnull;

abstract public class GoGenerateTestActionBase extends CodeInsightAction {
  @Nonnull
  private final GoTestFramework myFramework;
  @Nonnull
  private final CodeInsightActionHandler myHandler;

  protected GoGenerateTestActionBase(@Nonnull GoTestFramework framework, @Nonnull CodeInsightActionHandler handler) {
    myFramework = framework;
    myHandler = handler;
  }

  @Nonnull
  @Override
  protected CodeInsightActionHandler getHandler() {
    return myHandler;
  }

  @Override
  protected boolean isValidForFile(@Nonnull Project project, @Nonnull Editor editor, @Nonnull PsiFile file) {
    return myFramework.isAvailableOnFile(file);
  }
}
