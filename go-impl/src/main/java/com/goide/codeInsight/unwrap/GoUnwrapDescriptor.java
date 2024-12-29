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

package com.goide.codeInsight.unwrap;

import com.goide.GoLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.refactoring.unwrap.UnwrapDescriptorBase;
import consulo.language.editor.refactoring.unwrap.Unwrapper;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class GoUnwrapDescriptor extends UnwrapDescriptorBase {
  @Override
  protected Unwrapper[] createUnwrappers() {
    return new Unwrapper[]{
      new GoElseRemover(),
      new GoElseUnwrapper(),
      new GoIfUnwrapper(),
      new GoForUnwrapper(),
      new GoBracesUnwrapper(),
      new GoFunctionArgumentUnwrapper()
    };
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return GoLanguage.INSTANCE;
  }
}
