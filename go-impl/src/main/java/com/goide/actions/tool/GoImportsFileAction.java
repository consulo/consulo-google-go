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

package com.goide.actions.tool;

import com.goide.util.GoExecutor;
import consulo.annotation.component.ActionImpl;
import consulo.module.Module;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ActionImpl(id = "GoImportsFileAction")
public class GoImportsFileAction extends GoDownloadableFileAction {
  public GoImportsFileAction() {
    super("Goimports file", "Optimizes imports for selected file with goimports util", "goimports", "golang.org/x/tools/cmd/goimports");
  }

  @Nonnull
  @Override
  protected GoExecutor createExecutor(@Nonnull Project project, @Nullable Module module, @Nonnull String title, @Nonnull String filePath) {
    VirtualFile executable = getExecutable(project, module);
    assert executable != null;
    return GoExecutor.in(project, module).withExePath(executable.getPath()).withParameters("-w", filePath).showOutputOnError();
  }
}
