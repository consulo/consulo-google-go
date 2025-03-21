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

@ActionImpl(id = "GoTypeFileAction")
public class GoTypeFileAction extends GoDownloadableFileAction {
    public GoTypeFileAction() {
        super("Gotype directory", "Analyze types for current directory with gotype util", "gotype", "golang.org/x/tools/cmd/gotype");
    }

    @Override
    protected boolean isAvailableOnFile(VirtualFile file) {
        return super.isAvailableOnFile(file) || file.isDirectory();
    }

    @Nonnull
    @Override
    protected GoExecutor createExecutor(@Nonnull Project project, @Nullable Module module, @Nonnull String title, @Nonnull VirtualFile file) {
        return super.createExecutor(project, module, title, file.isDirectory() ? file : file.getParent());
    }

    @Nonnull
    @Override
    protected GoExecutor createExecutor(@Nonnull Project project, @Nullable Module module, @Nonnull String title, @Nonnull String filePath) {
        VirtualFile executable = getExecutable(project, module);
        assert executable != null;

        return GoExecutor.in(project, module).withExePath(executable.getPath()).withParameters("-e", "-a", "-v", filePath)
            .showNotifications(false, true).showOutputOnError();
    }
}
