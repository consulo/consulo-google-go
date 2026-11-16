/*
 * Copyright 2013-2026 consulo.io
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

package com.goide.psi.impl;

import com.goide.psi.GoFile;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.language.psi.PsiDirectory;
import org.jspecify.annotations.Nullable;

/**
 * Creates the {@link GoPackage} of a file or of directories, for build systems that define the files of a package themselves.
 * <p>
 * Factories are asked in their {@code order}; a {@code null} result means "not mine", and the directory-backed
 * {@link GoPackage} is used when no factory answers. Use {@link GoPackage#of(GoFile)} and {@link GoPackage#in(PsiDirectory, String)}
 * instead of calling factories directly.
 * <p>
 * Upstream this is {@code com.goide.project.GoPackageFactory}; the {@code com.goide.project} package belongs to the
 * consulo.go.toolchain module, which cannot see {@link GoFile}.
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface GoPackageFactory {
    @RequiredReadAction
    @Nullable GoPackage createPackage(GoFile goFile);

    @RequiredReadAction
    @Nullable GoPackage createPackage(String packageName, PsiDirectory... directories);
}
