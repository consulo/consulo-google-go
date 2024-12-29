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

package com.goide.runconfig.testing.ui;

import com.goide.completion.GoImportPathsCompletionProvider;
import com.goide.util.GoUtil;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.ui.awt.TextFieldCompletionProvider;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.Module;

import jakarta.annotation.Nonnull;
import java.util.function.Supplier;

public class GoPackageFieldCompletionProvider extends TextFieldCompletionProvider {
  @Nonnull
  private final Supplier<Module> myModuleProducer;

  public GoPackageFieldCompletionProvider(@Nonnull Supplier<Module> moduleProducer) {
    myModuleProducer = moduleProducer;
  }

  @Override
  public void addCompletionVariants(@Nonnull String text,
                                       int offset,
                                       @Nonnull String prefix,
                                       @Nonnull CompletionResultSet result) {
    Module module = myModuleProducer.get();
    if (module != null) {
      GlobalSearchScope scope = GoUtil.moduleScopeWithoutLibraries(module.getProject(), module);
      GoImportPathsCompletionProvider.addCompletions(result, module, null, scope, true);
    }
  }
}
